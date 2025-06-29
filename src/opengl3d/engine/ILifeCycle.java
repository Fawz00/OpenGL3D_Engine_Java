package opengl3d.engine;

public interface ILifeCycle {
    /**
     * Called when the object is created or initialized.
     */
    void awake();

    /**
     * Called once at the start of the object's lifecycle.
     */
    void start();

    /**
     * Called every frame to update the object's state.
     */
    void update();

    /**
     * Called at fixed intervals for physics updates or similar tasks.
     */
    void fixedUpdate();

    /**
     * Called when the object is rendered.
     */
    void render();

    /**
     * Called when the object is destroyed or cleaned up.
     */
    void destroy();
}
