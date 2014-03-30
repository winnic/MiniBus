package util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.InputType;
import android.widget.EditText;

public class FireMissilesDialogFragment extends DialogFragment {
	String type=null;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
        
        
        if((type=getArguments().getString("type"))!= null){
    		if(type=="connectionErr"){
    			builder.setTitle(getArguments().getString("err"));
    			builder.setMessage("Please input the server IP address");
    			// Set up the input
    	        final EditText input = new EditText(getActivity());
    	        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
    	        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
    	        builder.setView(input);
    	        
    			builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) { 
                    	final Pattern IP_ADDRESS = Pattern.compile(
                            "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4]"
                            + "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]"
                            + "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}"
                            + "|[1-9][0-9]|[0-9]))");
	                    Matcher matcher = IP_ADDRESS.matcher(input.getText().toString());
	                    
	                    if (matcher.matches()) {
	                    	SharedPreferences sharedPref = getActivity().getSharedPreferences("IP",Context.MODE_PRIVATE);
	                    	SharedPreferences.Editor editor = sharedPref.edit();
	                    	editor.putString("ip", input.getText().toString());
	                    	editor.commit();
	                    }

                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
    		}
        }else{
            builder.setMessage("dialog_fire_missiles")
            .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // FIRE ZE MISSILES!
                }
            })
            .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                }
            });
     // Create the AlertDialog object and return it
        }

        return builder.create();
    }
}