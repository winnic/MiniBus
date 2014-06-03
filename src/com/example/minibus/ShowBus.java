package com.example.minibus;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

import util.webService;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.actionbarsherlock.ActionBarSherlock;
import com.actionbarsherlock.view.Window;

public class ShowBus extends Activity {
	
	private int method = -1;
	private String buslist_str="";
	private String[] busList;
	private String[] S_E;
//	private String[] websites;
	private ActionBarSherlock mSherlock;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// progress dialog
        if (mSherlock == null) {
            mSherlock = ActionBarSherlock.wrap(this, ActionBarSherlock.FLAG_DELEGATE);
        }
        mSherlock.requestFeature((int) Window.FEATURE_INDETERMINATE_PROGRESS);
        mSherlock.setProgressBarIndeterminate(true);
		
		//TODO add search bar on the top
		
		if (getIntent().getExtras() != null) {
			method = getIntent().getExtras().getInt("method");
			buslist_str= getIntent().getExtras().getString("buslist_str");
			Log.w("testing","buslist_str is gotton successfully!");
			
			if(method==1){
				busList=buslist_str.split(" ");
			}else if(method==3){
				try {
					  JSONObject jsonObject = new JSONObject(buslist_str);
					  Iterator<Object> keys = jsonObject.keys();
					  busList=new String[jsonObject.length()];
					  S_E=new String[jsonObject.length()];
				       for(int i=0;keys.hasNext();i++) {
				        	busList[i]=String.valueOf(keys.next());
//				        	Log.w("testing","key = "+busList[i].toString());
				        	S_E[i]=(String) ((JSONObject)jsonObject.get(busList[i].toString())).get("takeUp")+";;"+(String) ((JSONObject)jsonObject.get(busList[i].toString())).get("takeOff");
//				        	Log.w("testing","value = "+S_E[i].toString());
				       }
				    } catch (Exception e) {
				      e.printStackTrace();
				    }
			}
			if(busList==null){
				busList=new String[1];
				busList[0]="no result matched";
			}
			Log.w("testing","busList.length="+busList.length);

//			websites=new String[busList.length];
//			for(int i = 0;i<busList.length;i++){
//				websites[i]=busList[i].split("\\n")[0];
//				busList[i]=busList[i].substring(websites[i].length()+1);
//			}
		}
			
		super.onCreate(savedInstanceState);
		
		setTitle("Searching Result");
//		
//		if (mPos == -1 && savedInstanceState != null)
//			mPos = savedInstanceState.getInt("mPos");
//
//		
//		Log.e("testing","mPos = "+mPos);
		
		//GridView gv = (GridView) inflater.inflate(R.layout.list_grid, null);
		setContentView(R.layout.list_grid);
		
		GridView gv = (GridView) this.findViewById(R.id.booksGrid);
		
		//gv.setBackgroundResource(android.R.color.black);
		gv.setAdapter(new GridAdapter());
		gv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
//				if (getActivity() == null)
//					return;
				onBookPressed(position,view);
			}			
		});
//!!delete
//		onBookPressed(557,gv);
	}
	
	private class GridAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return busList.length;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.grid_item, null);
			}
			TextView v = (TextView) convertView.findViewById(R.id.grid_item_text);
			v.setText(busList[position]);
			return convertView;
		}
		
	}
	
	public void onBookPressed(int pos,View view) {
		activateProgressBar(true);
		webService connectToUrl=new webService();
		String url="";
		if(method==3)
			url="/redmini/api.php?action=fetch_bus&bus="+busList[pos]+"&S_E="+S_E[pos];
		if(method==1)
			url="/redmini/api.php?action=fetch_bus&bus="+busList[pos]+"&S_E="+"0;;0";
		Log.v("testing","onBookPressed pos = "+pos+" && url = "+url);
		connectToUrl.contextStartBrowerTo_URL(this.getBaseContext(),view,url,2);
	}
	
	public void activateProgressBar(boolean activate){
		mSherlock.setProgressBarIndeterminateVisibility(activate);
	}

}
