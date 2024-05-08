package opengl3d.ui;

public class UIEvent {
  public static int EVENT_UNKNOW = -1;
  public static int EVENT_ON_RELEASE = 0;
  public static int EVENT_ON_CLICK = 1;
  public static int EVENT_ON_HOVER = 3;
  public static int EVENT_ON_NOT_HOVER = 4;

  public UIEvent() {}

  public void runOnClick() {}
  public void runOnRelease() {}
  public void runOnHover() {}
  public void runOnNotHover() {}
}
