package opengl3d.engine;

import java.util.ArrayList;
import java.util.List;

public class ComponentContainer {
    private List<Component> components = new ArrayList<Component>();

    /* ================================================= *
     *                LIFE CYCLE METHODS                 *
     * ================================================= */
    //#region Life Cycle Methods
    public void startAll() {
        for (Component component : new ArrayList<>(components)) {
            component.start();
        }
    }
    public void updateAll() {
        for (Component component : new ArrayList<>(components)) {
            component.update();
        }
    }
    public void fixedUpdateAll() {
        for (Component component : new ArrayList<>(components)) {
            component.fixedUpdate();
        }
    }
    //#endregion Life Cycle Methods

    /* ================================================= *
     *                COMPONENTS                         *
     * ================================================= */
    //#region Components

    public void add(Component component, GameObject owner) {
        Class<?> componentType = component.getClass();
        for (Component existingComponent : components) {
            if (existingComponent.getClass().isAssignableFrom(component.getClass())) {
                System.out.println("Component of type " + componentType + " already exists");
                return;
            }
        }
        component.setOwner(owner);
        components.add(component);
    }
    public <T extends Component> T get(Class<T> type) {
        for (Component component : components) {
            if (type.isInstance(component)) {
                return type.cast(component);
            }
        }
        return null;
    }
    public <I> I getIfImplements(Class<I> interfaceType) {
        for (Component component : components) {
            if(interfaceType.isInstance(component)) {
                return interfaceType.cast(component);
            }
        }
        return null;
    }
    public <T extends Component> void remove(Class<T> type) {
        for (Component component : components) {
            if (type.isInstance(component)) {
                component.remove();
                components.remove(component);
                return;
            }
        }
    }
    public void clear() {
        for (Component component : new ArrayList<>(components)) {
            component.remove();
        }
        components.clear();
    }
    public List<Component> getAll() {
        return components;
    }
    public boolean has(Class<? extends Component> type) {
        for (Component component : components) {
            if (type.isInstance(component)) {
                return true;
            }
        }
        return false;
    }
    //#endregion Components
}
