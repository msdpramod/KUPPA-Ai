package ai.kuppa.conversation;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class VayuRequestLifecycle {
    private final ConcurrentMap<String, AtomicBoolean> activeRequests = new ConcurrentHashMap<>();

    public boolean register(String correlationId) {
        return activeRequests.putIfAbsent(correlationId, new AtomicBoolean(false)) == null;
    }

    public boolean cancel(String correlationId) {
        AtomicBoolean cancelled = activeRequests.get(correlationId);
        return cancelled != null && cancelled.compareAndSet(false, true);
    }

    public boolean isCancelled(String correlationId) {
        AtomicBoolean cancelled = activeRequests.get(correlationId);
        return cancelled != null && cancelled.get();
    }

    public void release(String correlationId) {
        activeRequests.remove(correlationId);
    }
}
