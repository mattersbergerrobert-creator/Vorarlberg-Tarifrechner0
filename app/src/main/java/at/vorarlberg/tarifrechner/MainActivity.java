package at.vorarlberg.tarifrechner;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.content.Context;
import android.widget.*;
import android.view.View;
import android.text.TextUtils;
import java.util.List;

public class MainActivity extends Activity {
  TextView status;
  EditText from, to;
  @Override public void onCreate(Bundle state) {
    super.onCreate(state);
    LinearLayout root = new LinearLayout(this);
    root.setOrientation(LinearLayout.VERTICAL);
    root.setPadding(32,48,32,32);
    TextView title = new TextView(this);
    title.setText("Vorarlberg Tarifrechner");
    title.setTextSize(25);
    root.addView(title);
    status = new TextView(this);
    status.setText("Standort noch nicht ermittelt");
    status.setPadding(0,24,0,24);
    root.addView(status);
    Button gps = new Button(this);
    gps.setText("Meinen Standort ermitteln");
    root.addView(gps);
    from = new EditText(this);
    from.setHint("Startort / Haltestelle");
    root.addView(from);
    to = new EditText(this);
    to.setHint("Zielort / Haltestelle");
    root.addView(to);
    Button calculate = new Button(this);
    calculate.setText("Tarif prüfen");
    root.addView(calculate);
    TextView result = new TextView(this);
    result.setPadding(0,24,0,0);
    root.addView(result);
    calculate.setOnClickListener(v -> result.setText("Die verlässliche automatische Tarifberechnung ist noch in Entwicklung. Es wird kein ungeprüfter Preis angezeigt."));
    gps.setOnClickListener(v -> requestGps());
    setContentView(root);
    requestGps();
  }
  void requestGps() {
    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
        checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
      requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 10);
      return;
    }
    try {
      LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
      if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER) && !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
        status.setText("Bitte Standort in Android aktivieren."); return;
      }
      status.setText("Standort wird gesucht …");
      for (String provider : lm.getProviders(true)) {
        Location last = lm.getLastKnownLocation(provider);
        if (last != null) showLocation(last);
        lm.requestSingleUpdate(provider, new android.location.LocationListener() {
          @Override public void onLocationChanged(Location l) { showLocation(l); }
        }, null);
      }
    } catch (SecurityException e) { status.setText("Standortberechtigung fehlt."); }
      catch (Exception e) { status.setText("Standort konnte nicht ermittelt werden."); }
  }
  void showLocation(Location l) {
    runOnUiThread(() -> status.setText(String.format(java.util.Locale.GERMANY,
      "GPS: %.5f, %.5f (Genauigkeit ca. %.0f m)",l.getLatitude(),l.getLongitude(),l.getAccuracy())));
  }
  @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grants) {
    super.onRequestPermissionsResult(requestCode, permissions, grants);
    if (requestCode == 10) {
      for (int grant : grants) if (grant == PackageManager.PERMISSION_GRANTED) { requestGps(); return; }
      status.setText("Bitte Standortberechtigung erlauben.");
    }
  }
}
