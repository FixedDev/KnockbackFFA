package net.minebukket.knockbackpvp.exceptions;

/*******************************************************************************
 *  Copyright (C) SparkNetwork - All Rights Reserved
 *   * Unauthorized copying of this file, via any medium is strictly prohibited
 *   * Proprietary and confidential
 *   * Written by Gilberto Garcia <gilbertodamian14@gmail.com>, May 2018
 *
 ******************************************************************************/
public class MapLoadException extends Exception {
    public MapLoadException() {
    }

    public MapLoadException(String message) {
        super("Failed to load map, reason:" + message);
    }

    public MapLoadException(String message, Throwable cause) {
        super("Failed to load map, reason:" + message, cause);
    }

    public MapLoadException(Throwable cause) {
        super(cause);
    }

    public MapLoadException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super("Failed to load map, reason:" + message, cause, enableSuppression, writableStackTrace);
    }
}
