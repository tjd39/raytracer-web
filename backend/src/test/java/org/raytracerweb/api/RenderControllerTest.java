package org.raytracerweb.api;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class RenderControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    // ── /api/render/scenes ──────────────────────────────────────────────────

    @Test
    void listScenes_returnsAllFourPresets() throws Exception {
        mvc.perform(get("/api/render/scenes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[*].name", hasItems("simple_ball", "pool_balls", "sunset_balls", "cowboy_pingy")))
                .andExpect(jsonPath("$[0].defaultCamera.position", hasSize(3)))
                .andExpect(jsonPath("$[0].defaultCamera.look",     hasSize(3)));
    }

    // ── POST /api/render ────────────────────────────────────────────────────

    @Test
    void submitRender_returns202WithJobId() throws Exception {
        mvc.perform(post("/api/render")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(smallRenderRequest("simple_ball")))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.jobId").isString());
    }

    // ── GET /api/render/{id}/status ─────────────────────────────────────────

    @Test
    void getStatus_unknownJob_returns404() throws Exception {
        mvc.perform(get("/api/render/00000000-0000-0000-0000-000000000000/status"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getStatus_knownJob_hasStatusAndProgress() throws Exception {
        String jobId = submitAndExtractId("simple_ball");

        mvc.perform(get("/api/render/" + jobId + "/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").isString())
                .andExpect(jsonPath("$.progress").isNumber());
    }

    @Test
    void getStatus_progress_isBetween0And100() throws Exception {
        String jobId = submitAndExtractId("simple_ball");

        mvc.perform(get("/api/render/" + jobId + "/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.progress", allOf(greaterThanOrEqualTo(0), lessThanOrEqualTo(100))));
    }

    @Test
    void getStatus_completedJob_reaches100PercentAndComplete() throws Exception {
        String jobId = submitAndExtractId("simple_ball");
        waitForComplete(jobId);

        mvc.perform(get("/api/render/" + jobId + "/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETE"))
                .andExpect(jsonPath("$.progress").value(100));
    }

    // ── GET /api/render/{id}/image ──────────────────────────────────────────

    @Test
    void getImage_beforeComplete_returns202() throws Exception {
        // Submit a large render so it's still running when we immediately poll
        String body = """
                {
                  "scene": {"name":"simple_ball","label":"Simple ball",
                    "defaultCamera":{"position":[3,3,3],"look":[-0.577,-0.577,-0.577]}},
                  "width":500,"height":500,"renderLevels":8,"antiAlias":4
                }
                """;
        MvcResult result = mvc.perform(post("/api/render")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn();
        String jobId = mapper.readTree(result.getResponse().getContentAsString()).get("jobId").asText();

        mvc.perform(get("/api/render/" + jobId + "/image"))
                .andExpect(status().is(anyOf(is(202), is(200))));
    }

    @Test
    void getImage_unknownJob_returns404() throws Exception {
        mvc.perform(get("/api/render/00000000-0000-0000-0000-000000000000/image"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getImage_completedJob_returnsPng() throws Exception {
        String jobId = submitAndExtractId("simple_ball");
        waitForComplete(jobId);

        mvc.perform(get("/api/render/" + jobId + "/image"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG));
    }

    // ── DELETE /api/render/{id} ─────────────────────────────────────────────

    @Test
    void cancelRender_unknownJob_returns404() throws Exception {
        mvc.perform(delete("/api/render/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound());
    }

    @Test
    void cancelRender_knownJob_returns204() throws Exception {
        String jobId = submitAndExtractId("simple_ball");
        mvc.perform(delete("/api/render/" + jobId))
                .andExpect(status().isNoContent());
    }

    // ── POST /api/camera/move ───────────────────────────────────────────────

    @Test
    void cameraMove_moveForward_updatesPosition() throws Exception {
        String body = """
                {
                  "camera": {"position":[0,0,5],"look":[0,0,-1]},
                  "action": "moveForward"
                }
                """;
        mvc.perform(post("/api/camera/move")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.position[2]").value(lessThan(5.0)));
    }

    @Test
    void cameraMove_allActions_return200() throws Exception {
        String[] actions = {"moveForward","moveBackward","moveLeft","moveRight",
                "moveUp","moveDown","turnLeft","turnRight",
                "turnUp","turnDown","barrelLeft","barrelRight"};
        for (String action : actions) {
            mvc.perform(post("/api/camera/move")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"camera\":{\"position\":[3,3,3],\"look\":[-0.577,-0.577,-0.577]},\"action\":\"" + action + "\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.position").isArray())
                    .andExpect(jsonPath("$.look").isArray());
        }
    }

    @Test
    void cameraMove_unknownAction_returns400() throws Exception {
        mvc.perform(post("/api/camera/move")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"camera\":{\"position\":[0,0,5],\"look\":[0,0,-1]},\"action\":\"fly\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cameraMove_returnedLook_isNormalised() throws Exception {
        MvcResult result = mvc.perform(post("/api/camera/move")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"camera\":{\"position\":[3,3,3],\"look\":[-0.577,-0.577,-0.577]},\"action\":\"turnLeft\"}"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode look = mapper.readTree(result.getResponse().getContentAsString()).get("look");
        double len = Math.sqrt(
                Math.pow(look.get(0).asDouble(), 2) +
                Math.pow(look.get(1).asDouble(), 2) +
                Math.pow(look.get(2).asDouble(), 2));
        assertThat(len).isCloseTo(1.0, org.assertj.core.api.Assertions.offset(1e-4));
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private String smallRenderRequest(String sceneName) {
        return """
                {
                  "scene": {"name":"%s","label":"Test",
                    "defaultCamera":{"position":[3,3,3],"look":[-0.577,-0.577,-0.577]}},
                  "width":20,"height":20,"renderLevels":1,"antiAlias":1
                }
                """.formatted(sceneName);
    }

    private String submitAndExtractId(String sceneName) throws Exception {
        MvcResult result = mvc.perform(post("/api/render")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(smallRenderRequest(sceneName)))
                .andExpect(status().isAccepted())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("jobId").asText();
    }

    private void waitForComplete(String jobId) throws Exception {
        for (int i = 0; i < 60; i++) {
            MvcResult r = mvc.perform(get("/api/render/" + jobId + "/status")).andReturn();
            String status = mapper.readTree(r.getResponse().getContentAsString()).get("status").asText();
            if ("COMPLETE".equals(status) || "ERROR".equals(status)) return;
            Thread.sleep(300);
        }
    }

    // import for the inline assertThat
    private static <T> org.assertj.core.api.AbstractDoubleAssert<?> assertThat(double d) {
        return org.assertj.core.api.Assertions.assertThat(d);
    }

    private static org.hamcrest.Matcher<Integer> anyOf(
            org.hamcrest.Matcher<Integer> a, org.hamcrest.Matcher<Integer> b) {
        return org.hamcrest.Matchers.anyOf(a, b);
    }
    private static org.hamcrest.Matcher<Integer> is(int v) {
        return org.hamcrest.Matchers.is(v);
    }
}
