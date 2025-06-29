package opengl3d.engine;

import java.util.List;

import opengl3d.engine.annotations.CallByEngine;

/**
 * Abstract base class for all actors in the engine.
 * <p>
 * An ActorBase represents an entity in the game world that can have components attached to it,
 * and participates in the game loop via a set of life cycle methods. It manages its own components
 * through a {@link ComponentContainer} and provides hooks for initialization, updates, rendering,
 * and destruction.
 * </p>
 *
 * <h2>Life Cycle</h2>
 * <ul>
 *   <li>{@link #awake()} - Called when the actor is first created.</li>
 *   <li>{@link #start()} - Called before the first update, after all actors are initialized.</li>
 *   <li>{@link #update()} - Called every frame to update the actor's logic.</li>
 *   <li>{@link #fixedUpdate()} - Called at a fixed interval for physics or time-based updates.</li>
 *   <li>{@link #render()} - Called every frame to render the actor.</li>
 *   <li>{@link #destroy()} - Called when the actor is being destroyed.</li>
 * </ul>
 *
 * <h2>Component Management</h2>
 * <ul>
 *   <li>{@link #addComponent(Component)} - Adds a component to the actor.</li>
 *   <li>{@link #getComponent(Class)} - Retrieves a component by its type.</li>
 *   <li>{@link #getComponentIfImplements(Class)} - Retrieves a component by interface.</li>
 *   <li>{@link #removeComponent(Class)} - Removes a component by its type.</li>
 *   <li>{@link #clearComponents()} - Removes all components from the actor.</li>
 *   <li>{@link #getComponents()} - Returns a list of all attached components.</li>
 *   <li>{@link #hasComponent(Class)} - Checks if a component of a given type is attached.</li>
 * </ul>
 *
 * <h2>Backend Methods</h2>
 * <p>
 * Methods annotated with {@code @CallByEngine} are intended to be called by the engine's internal
 * systems and should not be called directly by user code.
 * </p>
 *
 * @see GameObject
 * @see ILifeCycle
 * @see Component
 * @see ComponentContainer
 * @see Level
 */
public abstract class ActorBase extends GameObject implements ILifeCycle {
    private Level currentLevel = null;
    private ComponentContainer componentContainer = new ComponentContainer();

    public ActorBase(Level level) {
        this.currentLevel = level;
    }

    public Level getCurrentLevel() {
        return currentLevel;
    }

    /* ================================================= *
     *             BACKEND INJECTION METHODS             *
     * ================================================= */
    //#region Backend Methods
    
    @CallByEngine
    private void onAwake() {
        awake();
    }

    @CallByEngine
    private void onStart() {
        componentContainer.startAll();
        start();
    }

    @CallByEngine
    private void onUpdate() {
        componentContainer.updateAll();
        update();
    }

    @CallByEngine
    private void onFixedUpdate() {
        componentContainer.fixedUpdateAll();
        fixedUpdate();
    }

    @CallByEngine
    private void onRender() {
        render();
    }

    @CallByEngine
    private void onDestroy() {
        destroy();
        componentContainer.clear();
    }
    //#endregion Backend Methods

    /* ================================================= *
     *                LIFE CYCLE METHODS                 *
     * ================================================= */
    //#region Life Cycle Methods

    @Override
    public void awake() {
        // Override this method to initialize the actor
    }

    @Override
    public void start() {
        // Override this method to start the actor
    }

    @Override
    public void update() {
        // Override this method to update the actor
    }

    @Override
    public void fixedUpdate() {
        // Override this method to perform fixed updates on the actor
    }

    @Override
    public void render() {
        // Override this method to render the actor
    }

    @Override
    public void destroy() {
        // Override this method to clean up the actor
    }
    //#endregion Life Cycle Methods

    /* ================================================= *
     *                COMPONENTS                         *
     * ================================================= */
    //#region Components
    
    // Adds a component to the actor, ensuring no duplicates of the same type
    public void addComponent(Component component) {
        componentContainer.add(component, this);
    }
    public <T extends Component> T getComponent(Class<T> type) {
        return componentContainer.get(type);
    }
    public <I> I getComponentIfImplements(Class<I> interfaceType) {
        return componentContainer.getIfImplements(interfaceType);
    }
    public <T extends Component> void removeComponent(Class<T> type) {
        componentContainer.remove(type);
    }
    public void clearComponents() {
        componentContainer.clear();
    }
    public List<Component> getComponents() {
        return componentContainer.getAll();
    }
    public boolean hasComponent(Class<? extends Component> type) {
        return componentContainer.has(type);
    }
    //#endregion Components
    
}
