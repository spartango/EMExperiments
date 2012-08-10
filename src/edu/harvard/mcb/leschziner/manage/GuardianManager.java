package edu.harvard.mcb.leschziner.manage;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.http.RouteMatcher;

/**
 * A Manager serves as an API endpoint to handle requests for information or
 * modifications of pipelines. It hands requests off to the appropriate guardian
 * where pipeline state is actually stored. Managers are responsible for
 * resource allocation and reporting.
 * 
 * @author spartango
 * 
 */
public class GuardianManager {
    private static final Vertx                vertx        = Vertx.newVertx();

    public static final int                   DEFAULT_PORT = 8082;

    private final Map<UUID, PipelineGuardian> guardians;

    // HTTP Server
    private final HttpServer                  server;
    private final RouteMatcher                routeMatcher;

    public GuardianManager() {
        this(DEFAULT_PORT);
    }

    public GuardianManager(int port) {
        guardians = new ConcurrentHashMap<UUID, PipelineGuardian>();
        routeMatcher = new RouteMatcher();
        server = vertx.createHttpServer();

        setupRoutes();
        setupServer(port);
    }

    private void setupRoutes() {
        routeMatcher.get("/pipeline/:uuid/status",
                         new Handler<HttpServerRequest>() {
                             @Override public void
                                     handle(HttpServerRequest arg0) {
                                 handleStatus(arg0);
                             }
                         });
        routeMatcher.get("/pipeline/:uuid/results",
                         new Handler<HttpServerRequest>() {
                             @Override public void
                                     handle(HttpServerRequest arg0) {
                                 handleResults(arg0);
                             }
                         });
        routeMatcher.post("/pipeline/:uuid/destroy",
                          new Handler<HttpServerRequest>() {
                              @Override public void
                                      handle(HttpServerRequest arg0) {
                                  handleDestroy(arg0);
                              }
                          });
        routeMatcher.post("/pipeline/create", new Handler<HttpServerRequest>() {
            @Override public void handle(HttpServerRequest arg0) {
                handleCreate(arg0);
            }
        });
    }

    private void setupServer(int port) {
        server.requestHandler(routeMatcher).listen(port);
    }

    public void handleStatus(HttpServerRequest request) {
        HttpServerResponse response = request.response;

        String guardianId = request.params().get("uuid");
        if (guardianId == null) {
            response.statusCode = 400;
            response.end();
            return;
        }

        PipelineGuardian guardian = getGuardians().get(UUID.fromString(guardianId));

        if (guardian == null) {
            response.statusCode = 404;
            response.end();
            return;
        }

        response.end(guardian.getStatusJSON());
    }

    public void handleResults(HttpServerRequest request) {
        HttpServerResponse response = request.response;

        String guardianId = request.params().get("uuid");
        if (guardianId == null) {
            response.statusCode = 400;
            response.end();
            return;
        }

        PipelineGuardian guardian = getGuardians().get(UUID.fromString(guardianId));

        if (guardian == null) {
            response.statusCode = 404;
            response.end();
            return;
        }

        String results = guardian.getResultsJSON();
        if (results == null) {
            response.statusCode = 403;
            response.end("{}");
            return;
        }
        response.end(results);
    }

    public void handleDestroy(HttpServerRequest request) {
        HttpServerResponse response = request.response;
        String guardianId = request.params().get("uuid");
        if (guardianId == null) {
            response.statusCode = 400;
            response.end();
            return;
        }
        UUID gid = UUID.fromString(guardianId);
        PipelineGuardian guardian = getGuardians().get(gid);
        if (guardian == null) {
            response.statusCode = 404;
            response.end();
            return;
        }
        guardian.destroy();
        guardians.remove(gid);
        response.end();
    }

    public void handleCreate(HttpServerRequest request) {
        final HttpServerResponse response = request.response;

        // Do something with it at the end
        request.bodyHandler(new Handler<Buffer>() {

            @Override public void handle(Buffer body) {
                // Grab the body
                String bodyText = body.getString(0, body.length());
                System.out.println("[BodyHandler]: Got "
                                   + body.length()
                                   + " bytes");
                // Allocate a guardian
                // Pass the pipeline parameters
                PipelineGuardian newGuardian = new PipelineGuardian();
                if (newGuardian.initialize(bodyText)) {
                    // Give the client a guardian ID
                    getGuardians().put(newGuardian.getUUID(), newGuardian);
                    response.end(newGuardian.getUUID().toString());
                } else {
                    response.statusCode = 400;
                    response.statusMessage = "Bad Pipeline Parameters";
                    response.end();
                }
            }
        });
    }

    public void close() {
        server.close();
    }

    public Map<UUID, PipelineGuardian> getGuardians() {
        return guardians;
    }
}
