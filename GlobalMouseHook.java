import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.HHOOK;
import com.sun.jna.platform.win32.WinUser.LowLevelMouseProc;
import com.sun.jna.platform.win32.WinUser.MSG;

public class GlobalMouseHook {
    private static final int WM_MOUSEMOVE = 0x0200;

    private final CursorPos position = new CursorPos(0, 0);
    private volatile boolean moved = false;
    private HHOOK hook;
    private double lastX = 0, lastY = 0;
    private boolean first = true;

    private final LowLevelMouseProc hookProc = (nCode, wParam, lParam) -> {
        if (nCode >= 0 && wParam.intValue() == WM_MOUSEMOVE) {
            double x = lParam.pt.x;
            double y = lParam.pt.y;

            if (first) {
                lastX = x;
                lastY = y;
                first = false;
            } else {
                double dx = x - lastX;
                double dy = lastY - y;

                if (dx != 0 || dy != 0) {
                    position.setX(x);
                    position.setY(y);
                    position.setVectorX(dx);
                    position.setVectorY(dy);
                    moved = true;
                }

                lastX = x;
                lastY = y;
            }
        }


        LPARAM lp = new LPARAM(Pointer.nativeValue(lParam.getPointer()));
        return User32.INSTANCE.CallNextHookEx(hook, nCode, wParam, lp);
    };

    public CursorPos getPosition() {
        return position;
    }

    public boolean hasMoved() {
        if (moved) {
            moved = false;
            return true;
        }
        return false;
    }

    public void run() {
        hook = User32.INSTANCE.SetWindowsHookEx(WinUser.WH_MOUSE_LL,hookProc,Kernel32.INSTANCE.GetModuleHandle(null),0);

        if (hook == null) {
            throw new RuntimeException("Failed to install mouse hook");
        }
        System.out.println("Global mouse hook installed.");

        MSG msg = new MSG();
        while (User32.INSTANCE.GetMessage(msg, null, 0, 0) != 0) {
            User32.INSTANCE.TranslateMessage(msg);
            User32.INSTANCE.DispatchMessage(msg);

        }
    }

    public void destroy() {
        if (hook != null) {

            User32.INSTANCE.UnhookWindowsHookEx(hook);

        }
    }

    public static void main(String[] args) {
        GlobalMouseHook hook = new GlobalMouseHook();

        Thread pollThread = new Thread(() -> {
            while (true) {
                if (hook.hasMoved()) {

                    System.out.println(hook.getPosition());
                }
            }
        });
        pollThread.start();

        hook.run();
    }
}