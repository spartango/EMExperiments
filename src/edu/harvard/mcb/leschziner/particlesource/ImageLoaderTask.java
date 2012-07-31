package edu.harvard.mcb.leschziner.particlesource;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

import loci.formats.FormatException;

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

public class ImageLoaderTask extends DistributedProcessingTask {
    private static final Vertx vertx = Vertx.newVertx();

    private final String       targetPath;
    private final String       imageQueueName;

    private transient Boolean  completed;

    public ImageLoaderTask(String target,
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

    @Override public void process() {
        completed = false;
        final BlockingQueue<Particle> loadedImages = DefaultStorageEngine.getStorageEngine()
                                                                         .getQueue(imageQueueName);

        try {
            final URL url = new URL(targetPath);

            final String filename = UUID.randomUUID().toString();

            // Allocate a file for the path
            vertx.fileSystem().open(filename,
                                    new AsyncResultHandler<AsyncFile>() {

                                        @Override public void
                                                handle(AsyncResult<AsyncFile> ar) {
                                            if (ar.exception == null) {
                                                final AsyncFile asyncFile = ar.result;
                                                // Pump from the request
                                                HttpClient client = vertx.createHttpClient()
                                                                         .setPort(url.getPort())
                                                                         .setHost(url.getHost())
                                                                         .setKeepAlive(false);
                                                client.getNow(targetPath,
                                                              new Handler<HttpClientResponse>() {

                                                                  @Override public void
                                                                          handle(HttpClientResponse response) {
                                                                      // Pump
                                                                      // the
                                                                      // body to
                                                                      // the
                                                                      // file
                                                                      Pump.createPump(response,
                                                                                      asyncFile.getWriteStream())
                                                                          .start();

                                                                      response.endHandler(new Handler<Void>() {
                                                                          @Override public void
                                                                                  handle(Void arg0) {
                                                                              asyncFile.close();
                                                                              // Make
                                                                              // a
                                                                              // particle
                                                                              // out
                                                                              // of
                                                                              // the
                                                                              // file
                                                                              Particle newParticle;
                                                                              try {
                                                                                  newParticle = Particle.fromFile(filename);
                                                                                  loadedImages.add(newParticle);
                                                                              } catch (IOException
                                                                                       | FormatException e) {
                                                                                  markError("Could not read the downloaded image",
                                                                                            e);
                                                                              }
                                                                              completed = true;
                                                                              completed.notifyAll();
                                                                          }

                                                                      });
                                                                  }
                                                              });

                                            }

                                        }

                                    });
            // Wait for completion
            while (!completed) {
                completed.wait();
            }
        } catch (MalformedURLException | InterruptedException e) {
            markError("Could not use URL because it is malformed", e);
        }

    }

}
