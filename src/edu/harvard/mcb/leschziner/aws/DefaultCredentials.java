package edu.harvard.mcb.leschziner.aws;

import org.jets3t.service.security.AWSCredentials;

public class DefaultCredentials {
    private static final String         awsAccessKey   = "YOUR_AWS_ACCESS_KEY";
    private static final String         awsSecretKey   = "YOUR_AWS_SECRET_KEY";

    private static final AWSCredentials awsCredentials = new AWSCredentials(awsAccessKey,
                                                                            awsSecretKey);

    public static String getAwsAccessKey() {
        return awsAccessKey;
    }

    public static String getAwsSecretKey() {
        return awsSecretKey;
    }

    public static AWSCredentials getAwsCredentials() {
        return awsCredentials;
    }

}
