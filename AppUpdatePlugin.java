package __PACKAGE__;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.SigningInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import androidx.core.content.FileProvider;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/** Descarga la APK nueva (con progreso) y abre el instalador de Android. */
@CapacitorPlugin(name = "AppUpdate")
public class AppUpdatePlugin extends Plugin {

    @PluginMethod
    public void download(final PluginCall call) {
        final String url = call.getString("url");
        if (url == null || url.isEmpty()) { call.reject("missing_url"); return; }
        new Thread(() -> {
            HttpURLConnection c = null;
            try {
                c = (HttpURLConnection) new URL(url).openConnection();
                c.setConnectTimeout(15000);
                c.setReadTimeout(30000);
                c.setInstanceFollowRedirects(true);
                c.connect();
                if (c.getResponseCode() != 200) { call.reject("http_" + c.getResponseCode()); return; }
                long total = c.getContentLengthLong();
                File f = new File(getContext().getCacheDir(), "update.apk");
                try (InputStream in = c.getInputStream(); FileOutputStream out = new FileOutputStream(f)) {
                    byte[] buf = new byte[16384];
                    long done = 0; int n, last = -1;
                    while ((n = in.read(buf)) > 0) {
                        out.write(buf, 0, n);
                        done += n;
                        if (total > 0) {
                            int p = (int) (done * 100 / total);
                            if (p != last) { last = p; JSObject o = new JSObject(); o.put("percent", p); notifyListeners("progress", o); }
                        }
                    }
                }
                if (!sameSigner(f)) { f.delete(); call.reject("signature_mismatch"); return; }
                call.resolve();
            } catch (Exception e) {
                call.reject(e.getMessage() == null ? "error" : e.getMessage());
            } finally {
                if (c != null) c.disconnect();
            }
        }).start();
    }

    /** Android no instala una APK encima de otra si la firma es distinta: lo detectamos antes de abrir el instalador. */
    private boolean sameSigner(File apk) {
        try {
            PackageManager pm = getContext().getPackageManager();
            int flag = Build.VERSION.SDK_INT >= 28 ? PackageManager.GET_SIGNING_CERTIFICATES : PackageManager.GET_SIGNATURES;
            Signature[] a = signers(pm.getPackageArchiveInfo(apk.getAbsolutePath(), flag));
            Signature[] b = signers(pm.getPackageInfo(getContext().getPackageName(), flag));
            if (a == null || b == null || a.length == 0 || b.length == 0) return true; // no se puede saber: decide Android
            for (Signature x : a) for (Signature y : b) if (x.equals(y)) return true;
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    private Signature[] signers(PackageInfo p) {
        if (p == null) return null;
        if (Build.VERSION.SDK_INT >= 28) {
            SigningInfo si = p.signingInfo;
            if (si == null) return null;
            return si.hasMultipleSigners() ? si.getApkContentsSigners() : si.getSigningCertificateHistory();
        }
        return p.signatures;
    }

    @PluginMethod
    public void install(PluginCall call) {
        Context ctx = getContext();
        if (Build.VERSION.SDK_INT >= 26 && !ctx.getPackageManager().canRequestPackageInstalls()) {
            Intent s = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + ctx.getPackageName()));
            s.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(s);
            call.reject("need_permission");
            return;
        }
        File f = new File(ctx.getCacheDir(), "update.apk");
        if (!f.exists()) { call.reject("no_file"); return; }
        Uri u = FileProvider.getUriForFile(ctx, ctx.getPackageName() + ".fileprovider", f);
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(u, "application/vnd.android.package-archive");
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(i);
        call.resolve();
    }
}
