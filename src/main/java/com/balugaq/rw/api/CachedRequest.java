package com.balugaq.rw.api;

import org.jetbrains.annotations.NotNull;

public record CachedRequest(@NotNull Runnable request) {
    public void execute() {
        request.run();
    }
}
