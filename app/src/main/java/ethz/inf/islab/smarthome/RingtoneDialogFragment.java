package ethz.inf.islab.smarthome;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;

/**
 * Created by kentsay on 5/4/15.
 */
public class RingtoneDialogFragment extends DialogFragment {
    MediaPlayer mp;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Uri ringtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        mp = MediaPlayer.create(getActivity(), ringtone);
        mp.start();

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.ringtone_active)
                .setPositiveButton(R.string.ringtone_confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mp.stop();
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
