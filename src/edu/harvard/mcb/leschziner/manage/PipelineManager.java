package edu.harvard.mcb.leschziner.manage;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.vertx.java.core.Handler;
import org.vertx.java.core.SimpleHandler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;

/**
 * A Manager serves as an API endpoint to handle requests for information or
 * modifications of pipelines. It hands requests off to the appropriate guardian
 * where pipeline state is actually stored. Managers are responsible for
 * resource allocation and reporting.
 * 
 * @author spartango
 * 
 */
public class PipelineManager implements Handler<HttpServerRequest> {
    private final Map<UUID, PipelineGuardian> guardians;

    public PipelineManager() {
        guardians = new ConcurrentHashMap<UUID, PipelineGuardian>();
    }

    @Override public void handle(HttpServerRequest request) {
        // Handle request types
        if (request.method.equals("GET")) {
            handleGet(request);
        } else if (request.method.equals("POST")) {
            handlePost(request);
        }
    }

    private void handleGet(HttpServerRequest request) {
        HttpServerResponse response = request.response;
        if (request.uri.startsWith("/pipeline/")) {
            // Request status information
            // Pull out the id
            UUID guardianId = parsePipelineUUID(request.uri);

            if (guardianId != null && guardians.containsKey(guardianId)) {
                PipelineGuardian guardian = guardians.get(guardianId);

                if (request.uri.endsWith("/status")) {
                    response.write(guardian.getStatusJSON());
                } else if (request.uri.endsWith("/results")) {
                    response.write(guardian.getResultsJSON());
                } else {
                    response.statusCode = 400;
                    response.statusMessage = "Bad Request: No such operation";
                }

            } else {
                response.statusCode = 404;
                response.statusMessage = "Not Found: No such Pipeline";
            }
        } else {
            response.statusCode = 400;
            response.statusMessage = "Bad Request: No such control domain";
        }
        response.end();
    }

    private void handlePost(HttpServerRequest request) {
        HttpServerResponse response = request.response;
        if (request.uri.equals("/pipeline/create")) {
            final Buffer body = new Buffer(0);

            // Accumulate the body
            request.bodyHandler(new Handler<Buffer>() {

                @Override public void handle(Buffer buffer) {
                    body.appendBuffer(buffer);
                }
            });

            // Do something with it at the end
            request.endHandler(new SimpleHandler() {

                @Override protected void handle() {
                    // Grab the body
                    String bodyText = body.getString(0, body.length());
                    // Allocate a guardian
                    // Pass the pipeline parameters
                    PipelineGuardian newGuardian = new PipelineGuardian();
                    if (newGuardian.initialize(bodyText)) {
                        // Give the client a guardian ID
                        guardians.put(newGuardian.getUUID(), newGuardian);
                    }
                }
            });
        } else {
            response.statusCode = 400;
        }
        response.end();
    }

    private static UUID parsePipelineUUID(String uri) {
        String[] parts = uri.split("/");
        if (parts.length > 2) {
            try {
                return UUID.fromString(parts[2]);
            } catch (IllegalArgumentException e) {
                System.err.println("Error parsing UUID from request");
            }
        }
        return null;
    }

}
