package UDP_Client_Server.util;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;

public class KeyBoard implements KeyListener {

    /** This array says for every key, if the key is pressed at a given moment in time **/
    private boolean[] keysPressed;
    public static final int MAX_KEYCODE = 65489;
    public KeyBoard() {
        keysPressed = new boolean[MAX_KEYCODE+1];
        Arrays.fill(keysPressed, false);
    }
    public boolean isKeyPressed(int keyCode) {
        if (keyCode >= 0 && keyCode < keysPressed.length) {
            return keysPressed[keyCode];
        }
        return false;
    }
    public void keyTyped(KeyEvent e) {}
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode >= 0 && keyCode < keysPressed.length) {
            keysPressed[keyCode] = true;
        }
    }
    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode >= 0 && keyCode < keysPressed.length) {
            keysPressed[keyCode] = false;
        }
    }
}