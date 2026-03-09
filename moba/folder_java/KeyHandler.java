package folder_java;

import java.awt.event.*;

public class KeyHandler implements KeyListener {
    public boolean upPressed, downPressed, leftPressed, rightPressed, uPressed, iPressed, oPressed, rPressed,
            plusPressed, minusPressed, fPressed;

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_W)
            upPressed = true;
        if (code == KeyEvent.VK_S)
            downPressed = true;
        if (code == KeyEvent.VK_A)
            leftPressed = true;
        if (code == KeyEvent.VK_D)
            rightPressed = true;
        if (code == KeyEvent.VK_U)
            uPressed = true;
        if (code == KeyEvent.VK_I)
            iPressed = true;
        if (code == KeyEvent.VK_O)
            oPressed = true;
        if (code == KeyEvent.VK_R)
            rPressed = true;
        if (code == KeyEvent.VK_EQUALS || code == KeyEvent.VK_ADD)
            plusPressed = true;
        if (code == KeyEvent.VK_MINUS || code == KeyEvent.VK_SUBTRACT)
            minusPressed = true;
        if (code == KeyEvent.VK_F)
            fPressed = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_W)
            upPressed = false;
        if (code == KeyEvent.VK_S)
            downPressed = false;
        if (code == KeyEvent.VK_A)
            leftPressed = false;
        if (code == KeyEvent.VK_D)
            rightPressed = false;
        if (code == KeyEvent.VK_U)
            uPressed = false;
        if (code == KeyEvent.VK_I)
            iPressed = false;
        if (code == KeyEvent.VK_O)
            oPressed = false;
        if (code == KeyEvent.VK_R)
            rPressed = false;
        if (code == KeyEvent.VK_EQUALS || code == KeyEvent.VK_ADD)
            plusPressed = false;
        if (code == KeyEvent.VK_MINUS || code == KeyEvent.VK_SUBTRACT)
            minusPressed = false;
        if (code == KeyEvent.VK_F)
            fPressed = false;
    }
}
