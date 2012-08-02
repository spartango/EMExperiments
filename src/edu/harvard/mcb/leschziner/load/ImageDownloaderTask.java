package edu.harvard.mcb.leschziner.load;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.file.AsyncFile;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.streams.Pump;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.distributed.DistributedProcessingTask;
import edu.harvard.mcb.leschziner.storage.DefaultStorageEngine;

public class ImageDownloaderTask extends DistributedProcessingTask {
    public static final Vertx vertx = Vertx.newVertx();

    private final String      targetPath;
    private final String      imageQueueName;
    public transient Boolean  completed;

    public ImageDownloaderTask(String target,
                           String imageQueueName,
                           String executorName) {
        super(executorName);
        this.targetPath = target;
        this.imageQueueName = imageQueueName;
    }

    /**
     * 
     */
    private static final long serialVersionUID = 2452347990745320758L;

    private static final long POLL_TIME        = 100;                 // ms

    @Override public synchronized void process() {
        final BlockingQueue<Particle> loadedImages = DefaultStorageEngine.getStorageEngine()
                                                                         .getQueue(imageQueueName);

        completed = false;
        try {
            final URL url = new URL(getTargetPath());

            final String filename = "download/" + UUID.randomUUID().toString();

            // Allocate a file for the path
            vertx.fileSystem().open(filename,
                                    new AsyncResultHandler<AsyncFile>() {

                                        @Override public void
                                                handle(AsyncResult<AsyncFile> ar) {
                                            if (ar.exception == null) {
                                                final AsyncFile asyncFile = ar.result;
                                                // Pump from the request
                                                HttpClient client = vertx.createHttpClient()
                                                                         .setPort(url.getPort() > 0 ? url.getPort()
                                                                                                   : url.getDefaultPort())
                                                                         .setHost(url.getHost())
                                                                         .setKeepAlive(false);
                                                if (url.getProtocol()
                                                       .equals("https")) {
                                                    client.setSSL(true)
                                                          .setTrustAll(true);
                                                }
                                                System.out.println("["
                                                                   + this
                                                                   + "]: Loading Image from "
                                                                   + url.toString());

                                                client.getNow(getTargetPath(),
                                                              new Handler<HttpClientResponse>() {

                                                                  @Override public void
                                                                          handle(HttpClientResponse response) {
                                                                      Pump.createPump(response,
                                                                                      asyncFile.getWriteStream())
                                                                          .start();

                                                                      response.endHandler(new Handler<Void>() {
                                                                          @Override public void
                                                                                  handle(Void arg0) {
                                                                              asyncFile.close();

                                                                              Particle newParticle;
                                                                              try {
                                                                                  newParticle = Particle.fromFile(filename);
                                                                                  loadedImages.add(newParticle);
                                                                              } catch (IOException e) {
                                                                                  markError("Could not read the downloaded image",
                                                                                            e,
                                                                                            99);
                                                                              }
                                                                              completed = true;
                                                                          }

                                                                      });
                                                                  }
                                                              });

                                            }

                                        }

                                    });
            // Wait for completion
            while (!completed) {
                Thread.sleep(POLL_TIME);
            }
        } catch (MalformedURLException | InterruptedException e) {
            markError("Could not use URL because it is malformed", e, 118);
        }

    }

    public String getTargetPath() {
        return targetPath;
    }
}
