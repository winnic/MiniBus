package fragments;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import util.webService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.minibus.R;
import com.example.minibus.SearchPanel;

public class SearchContentFragment extends Fragment{
	
	private LinearLayout lines;
    private Spinner cuhkLibs_spinner;
    private Button searchBtn;
	private EditText keyWords;
    
    private TextToSpeech tts;
    private Button inputButton;    
    private static final int REQUEST_CODE = 1234;
    private ArrayList<String> matches;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		lines= (LinearLayout) inflater.inflate(R.layout.spinner, container, false);
		lines.setBackgroundResource(android.R.color.white);
		Log.v("testing", "SearchContentFragment onCreateView ");

		cuhkLibs_spinner=(Spinner) lines.findViewById(R.id.cuhkLibs_spinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity().getBaseContext(),
				R.array.cuhkLibs, R.layout.sherlock_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		cuhkLibs_spinner.setAdapter(adapter);
       
		final TextView output=(TextView) lines.findViewById(R.id.output);
//		output.setMovementMethod(new ScrollingMovementMethod());
		
		searchBtn=(Button) lines.findViewById(R.id.searchBtn);
		searchBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String url="/redmini/api.php?action=Select_Bus";
				((SearchPanel) getActivity()).activateProgressBar(true);
//				String tmp=keyWords.getText().toString();
//				String[] words=tmp.split(" ");
//				String searchKeyWords="";
//				for(int i=0;i<words.length-1;i++){
//					searchKeyWords+=words[i]+"+";
//				}
//				Log.v("testing", "searchKeyWords ="+searchKeyWords);
//				searchKeyWords+=words[words.length-1];

				webService connectToUrl=new webService();
				connectToUrl.contextStartBrowerTo_URL(getActivity().getBaseContext(),output,url,1);
				Log.v("testing", "output succeeds");
			}
		});
		
		keyWords=(EditText) lines.findViewById(R.id.keyWords);
		
		/////////////////////////////////////////////////
		setupTts_to_setSelectedText();
		/////////////////////////////////////////////////
		
		

		Log.v("testing", "SearchContentFragment onCreateView ends ");
		//!!
//		searchBtn.performClick();
		return lines;
	}
	
	
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
    	for(int i=0;i<matches.size();i++){
    		menu.add(matches.get(i));
    	}
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
    	keyWords.setText(item.toString(),TextView.BufferType.EDITABLE);
        return true;
    }
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	////////////////          TTS methods
	
	 private void setupTts_to_setSelectedText() {
       inputButton= (Button) lines.findViewById(R.id.inputButton);
       inputButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View arg0) {
           	 Log.v("testing", "inputButton setOnClickListener");
           	 startVoiceRecognitionActivity();
           }

       });
		
		tts = new TextToSpeech(getActivity(), new TextToSpeech.OnInitListener() {
			    @Override
			    public void onInit(int status){
			    	myTtsInit(status);
			    }
			});		
		
		registerForContextMenu(lines);
		lines.setLongClickable(false);
	}

	private void myTtsInit(int status){
		if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("testing", "This Language is not supported");
            }             
        } else {
            Log.e("testing", "Initilization Failed!");
        }
        
        // Disable button if no recognition service is present
        PackageManager pm = getActivity().getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities.size() == 0)
        {
        	inputButton.setEnabled(false);
        	inputButton.setText("Recognizer not present");
        } else{
        	 Log.v("testing", "Recognition service is present and num of service = "+activities.size());
        }
	}
	
    /**
     * Fire an intent to start the voice recognition activity.
     */
    private void startVoiceRecognitionActivity()
    {
    	Log.v("testing", "startVoiceRecognitionActivity");
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        startActivityForResult(intent, REQUEST_CODE);
    }
    
    /**
     * Handle the results from the voice recognition activity.
     */
    @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_CODE && resultCode == -1)
        {
            // Populate the wordsList with the String values the recognition engine thought it heard
            matches = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
   
            lines.showContextMenu();//!! call context menu by method
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
 
	////////////			TTS ends

}