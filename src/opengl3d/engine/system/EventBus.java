package opengl3d.engine.system;

import java.util.*;

/**
 * EventBus is a simple publish-subscribe event system for managing event listeners and dispatching events.
 * <p>
 * Listeners can subscribe to specific event types and will be notified when an event of that type is published.
 * </p>
 *
 * <ul>
 *   <li>{@link #subscribe(Class, EventListener)} - Registers a listener for a specific event type.</li>
 *   <li>{@link #unsubscribe(Class, EventListener)} - Unregisters a listener from a specific event type.</li>
 *   <li>{@link #publish(Object)} - Publishes an event to all listeners subscribed to its type.</li>
 *   <li>{@link #clear()} - Removes all registered listeners.</li>
 * </ul>
 *
 * <p>
 * Usage example:
 * <p>
 * <strong>Event Class</strong>
 * <pre>
 * public class DamageEvent {
 *     public final int damageAmount;
 *     public final int targetId;
 *
 *     public DamageEvent(int damageAmount, int targetId) {
 *         this.damageAmount = damageAmount;
 *         this.targetId = targetId;
 *     }
 * }
 * </pre>
 * <p>
 * <strong>Send Event</strong>
 * <pre>
 * EventBus.publish(new DamageEvent(10, playerId));
 * </pre>
 * <p>
 * <strong>Add Listener</strong>
 * <pre>
 * EventBus.subscribe(DamageEvent.class, event -> {
 *     System.out.println("Received damage: " + event.damageAmount);
 * });
 * </pre>
 *
 * @author YourName
 */
public class EventBus {
    private static final Map<Class<?>, List<EventListener<?>>> listeners = new HashMap<>();

    // Adding listener
    public static <T> void subscribe(Class<T> eventType, EventListener<T> listener) {
        listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(listener);
    }

    // Removing listener
    public static <T> void unsubscribe(Class<T> eventType, EventListener<T> listener) {
        List<EventListener<?>> eventListeners = listeners.get(eventType);
        if (eventListeners != null) {
            eventListeners.remove(listener);
        }
    }

    // Publishing event
    @SuppressWarnings("unchecked")
    public static <T> void publish(T event) {
        List<EventListener<?>> eventListeners = listeners.get(event.getClass());
        if (eventListeners != null) {
            for (EventListener<?> listener : eventListeners) {
                ((EventListener<T>) listener).onEvent(event);
            }
        }
    }

    // Clearing all listeners
    public static void clear() {
        listeners.clear();
    }

    // Interface for generic event listeners
    public interface EventListener<T> {
        void onEvent(T event);
    }
}
