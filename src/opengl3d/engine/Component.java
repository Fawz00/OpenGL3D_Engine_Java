package opengl3d.engine;

public abstract class Component {
    protected volatile GameObject owner;
    
    public void setOwner(GameObject e) {
        this.owner = e;
    }
    
    public GameObject getOwner() {
        return owner;
    }
    
    public abstract void start();
    public abstract void update();
    public abstract void fixedUpdate();
    public abstract void remove();
}
