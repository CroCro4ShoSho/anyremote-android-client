//
// anyRemote android client
// a bluetooth/wi-fi remote control for Linux.
//
// Copyright (C) 2011-2012 Mikhail Fedotov <anyremote@mail.ru>
// 
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA. 
//

package anyremote.client.android;

import java.util.Vector;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.TableLayout;
import android.widget.TextView;
import android.text.method.ScrollingMovementMethod;
import android.view.Display;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.KeyEvent;
import anyremote.client.android.util.ProtocolMessage;
import anyremote.client.android.util.arHandler;

public class ControlScreen extends arActivity 
                           implements View.OnClickListener,
                                      //View.OnTouchListener,
                                      KeyEvent.Callback {
  
   	static final int KEY_NUM1  = 1;
	static final int KEY_NUM2  = 2;
	static final int KEY_NUM3  = 3;
	static final int KEY_NUM4  = 4;
	static final int KEY_NUM5  = 5;
	static final int KEY_NUM6  = 6;
	static final int KEY_NUM7  = 7;
	static final int KEY_NUM8  = 8;
	static final int KEY_NUM9  = 9;
	static final int KEY_STAR  = 10;
	static final int KEY_NUM0  = 0;
	static final int KEY_POUND = 12;
	static final int KEY_UNKNOWN = -1;
	
   	static final String STR_NUM1  = "1";
	static final String STR_NUM2  = "2";
	static final String STR_NUM3  = "3";
	static final String STR_NUM4  = "4";
	static final String STR_NUM5  = "5";
	static final String STR_NUM6  = "6";
	static final String STR_NUM7  = "7";
	static final String STR_NUM8  = "8";
	static final String STR_NUM9  = "9";
	static final String STR_STAR  = "*";
	static final String STR_NUM0  = "0";
	static final String STR_POUND = "#";
	static final String STR_UNKNOWN = "";
	
	static final int SK_DEFAULT    = 0;
    static final int SK_BOTTOMLINE = 1;
   
    static final int NUM_ICONS     = 12;
    static final int NUM_ICONS_BTM = 7;
    
    boolean fullscreen  = false;
    boolean useJoystick = false;
    Dispatcher.ArHandler hdlLocalCopy;
    
    ImageButton [] buttons;
      
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);  
		
		prefix = "ControlScreen"; // log stuff
		log("onCreate");
		
		buttons = new ImageButton[NUM_ICONS];
	  	
	    hdlLocalCopy = new Dispatcher.ArHandler(anyRemote.CONTROL_FORM, new arHandler(this));
	    anyRemote.protocol.addMessageHandler(hdlLocalCopy);	    
	}
	
	/*@Override
	protected void onStart() {
		log("onStart");		
		super.onStart();
	}*/
	
	@Override
	protected void onPause() {
		log("onPause");	
		
		popup();
		
		//MainLoop.disable();
	    super.onPause();	
	}

	@Override
	protected void onResume() {
		log("onResume");		
		//MainLoop.enable();
		super.onResume();
		
        if (anyRemote.status == anyRemote.DISCONNECTED) {
        	log("onResume no connection");	
        	doFinish("");
        }
 
        redraw();
		popup();

	}
	
	@Override
	protected void onStop() {
		log("onStop");		
	    super.onStop();	
	}

	@Override
	protected void onDestroy() {	
		log("onDestroy");		
    	anyRemote.protocol.removeMessageHandler(hdlLocalCopy);
		super.onDestroy();
	}

    public void handleEvent(ProtocolMessage data) {
	   
    	log("handleEvent "+ data.id);
		
		if (data.stage != ProtocolMessage.FULL &&	// process only full commands
		    data.stage == ProtocolMessage.FIRST) {
			return;
		}
		
	    if (handleCommonCommand(data.id)) {
			return;
		}
	    
	    if (data.id == Dispatcher.CMD_VOLUME) {
    	    Toast.makeText(this, "Volume is "+anyRemote.protocol.cfVolume +"%", Toast.LENGTH_SHORT).show();
		    return;
		}

	    redraw();
    }
    
    private void setTitleField() {
    	//log("setTitleField "+anyRemote.protocol.cfTitle);
    	
    	int id = R.id.cf_title;
        if (anyRemote.protocol.cfSkin == SK_BOTTOMLINE) {
	        id = R.id.cf_btitle;
        } 
	    TextView title = (TextView) findViewById(id);
	    
	    //title.setMovementMethod(new ScrollingMovementMethod());
	    title.setText(anyRemote.protocol.cfTitle);
    }
    
    private void setStatusField() {
    	//log("setStatusField "+anyRemote.protocol.cfStatus);
    	
    	int id = R.id.cf_status;
        if (anyRemote.protocol.cfSkin == SK_BOTTOMLINE) {
        	id = R.id.cf_bstatus;
        } 
        TextView status = (TextView) findViewById(id);
        //status.setMovementMethod(new ScrollingMovementMethod());
        
    	status.setText(anyRemote.protocol.cfStatus);
    }
    	
	private void setSkinSimple() {
		
		Display display = getWindowManager().getDefaultDisplay();
		
		boolean rotated = (display.getOrientation() == Surface.ROTATION_90 ||
		                   display.getOrientation() == Surface.ROTATION_270);
		int h = (rotated ? display.getWidth()  : display.getHeight());
		int w = (rotated ? display.getHeight() : display.getWidth());
		//log("setSkin SCR "+rotated+" "+w+"x"+h);

		if (anyRemote.protocol.cfSkin == SK_BOTTOMLINE) {
			
			//log("setSkin SK_BOTTOMLINE");
			
			setContentView(R.layout.control_form_bottomline);
			
			buttons[0]  = (ImageButton) findViewById(R.id.bb1);
			buttons[1]  = (ImageButton) findViewById(R.id.bb2);
			buttons[2]  = (ImageButton) findViewById(R.id.bb3);
			buttons[3]  = (ImageButton) findViewById(R.id.bb4);
			buttons[4]  = (ImageButton) findViewById(R.id.bb5);
			buttons[5]  = (ImageButton) findViewById(R.id.bb6);
			buttons[6]  = (ImageButton) findViewById(R.id.bb7);
			buttons[7]  = null;
			buttons[8]  = null;
			buttons[9]  = null;
			buttons[10] = null;
			buttons[11] = null;
			
	        int realCnt = 0;
			for (int i=0;i<NUM_ICONS_BTM;i++) {
				Bitmap ic = anyRemote.getIconBitmap(getResources(), anyRemote.protocol.cfIcons[i]);
				
				buttons[i].setImageBitmap(ic);
				
				if (ic == null) {
					buttons[i].setVisibility(View.GONE);
				} else {
					buttons[i].setVisibility(View.VISIBLE);
				    realCnt++;
				}
				//buttons[i].setOnTouchListener(this);
				buttons[i].setOnClickListener(this);
			}
			
			for (int i=0;i<NUM_ICONS_BTM;i++) {
				buttons[i].setMaxHeight(w/realCnt);    	
				buttons[i].setMaxWidth (w/realCnt);
			}
			
			int sz = (w > h ? h : w);
			ImageView cover = (ImageView) findViewById(R.id.cover);
			cover.setMaxHeight((2*sz)/3);    	
			cover.setMaxWidth ((2*sz)/3);
			cover.setBackgroundColor(anyRemote.protocol.cfBkgr);
			
			if (anyRemote.protocol.cfInitFocus > 0 && anyRemote.protocol.cfInitFocus < NUM_ICONS_BTM) {
				buttons[anyRemote.protocol.cfInitFocus-1].requestFocus();
				buttons[anyRemote.protocol.cfInitFocus-1].requestFocusFromTouch();
			}
		} else {
			
			//log("setSkin SK_DEFAULT");
			
		    setContentView(R.layout.control_form_default);

			buttons[0]  = (ImageButton) findViewById(R.id.b1);
			buttons[1]  = (ImageButton) findViewById(R.id.b2);
			buttons[2]  = (ImageButton) findViewById(R.id.b3);
			buttons[3]  = (ImageButton) findViewById(R.id.b4);
			buttons[4]  = (ImageButton) findViewById(R.id.b5);
			buttons[5]  = (ImageButton) findViewById(R.id.b6);
			buttons[6]  = (ImageButton) findViewById(R.id.b7);
			buttons[7]  = (ImageButton) findViewById(R.id.b8);
			buttons[8]  = (ImageButton) findViewById(R.id.b9);
			buttons[9]  = (ImageButton) findViewById(R.id.b10);
			buttons[10] = (ImageButton) findViewById(R.id.b0);
			buttons[11] = (ImageButton) findViewById(R.id.b11);
			
			h = h/6;   // 4 rows with icons and 2 line of text + gaps
			w = w/3;   // 3 columns with icons
			int sz = (w > h ? h : w);

			for (int i=0;i<NUM_ICONS;i++) {
				
				Bitmap ic = anyRemote.getIconBitmap(getResources(), anyRemote.protocol.cfIcons[i]);
				if (ic == null) { // no to squeze
					ic = anyRemote.getIconBitmap(getResources(), "transparent"); 
				}
				
				buttons[i].setImageBitmap(ic);
								
				buttons[i].setVisibility(View.VISIBLE);
				buttons[i].setOnClickListener(this);
				
				buttons[i].setMaxHeight(sz);    	
				buttons[i].setMaxWidth (sz);
			}
			if (anyRemote.protocol.cfInitFocus > 0 && anyRemote.protocol.cfInitFocus < NUM_ICONS_BTM) {
				buttons[anyRemote.protocol.cfInitFocus-1].requestFocus();
				buttons[anyRemote.protocol.cfInitFocus-1].requestFocusFromTouch();
			}
		}
	}
	
	private void redraw() {
		
		anyRemote.protocol.setFullscreen(this);
		
		setTitle(anyRemote.protocol.cfCaption);
		
	    setSkinSimple();
		setFont();
		setTextColor();
	    setBackground();
	    
		setTitleField();
		setStatusField();
		
		setCover();
	}
	
	private void setBackground() {
		
		int id = R.id.skin_default;
		int maxIcon = NUM_ICONS;
		if (anyRemote.protocol.cfSkin == SK_BOTTOMLINE) {
	        id = R.id.skin_bottomline;
	        maxIcon = NUM_ICONS_BTM;
	        
	        ImageView cover = (ImageView) findViewById(R.id.cover);
	        cover.setBackgroundColor(anyRemote.protocol.cfBkgr);
	        
	        RelativeLayout ll = (RelativeLayout) findViewById(id);
		    ll.setBackgroundColor(anyRemote.protocol.cfBkgr);

		} else {
            LinearLayout ll = (LinearLayout) findViewById(id);
	        ll.setBackgroundColor(anyRemote.protocol.cfBkgr);
	        
	        TableLayout tl = (TableLayout) findViewById(R.id.icons);
	        tl.setBackgroundColor(anyRemote.protocol.cfBkgr);
		}
	}
	
	private void setTextColor() {
		
		int t = R.id.cf_title;
		int s = R.id.cf_status;
		if (anyRemote.protocol.cfSkin == SK_BOTTOMLINE) {
	       t = R.id.cf_btitle;
	       s = R.id.cf_bstatus;
		} 		
		TextView title  = (TextView) findViewById(t);
		TextView status = (TextView) findViewById(s);
		
	    title.setTextColor (anyRemote.protocol.cfFrgr);
	    status.setTextColor(anyRemote.protocol.cfFrgr);
	}
	
	private void setCover() {
		
		if (anyRemote.protocol.cfSkin == SK_BOTTOMLINE && anyRemote.protocol.cfCover != null) {
			ImageView cover  = (ImageView) findViewById(R.id.cover);
			cover.setImageBitmap(anyRemote.protocol.cfCover);
		}
	}
		
    private void setFont() {
       	
		int t = R.id.cf_title;
		int s = R.id.cf_status;
		if (anyRemote.protocol.cfSkin == SK_BOTTOMLINE) {
	       t = R.id.cf_btitle;
	       s = R.id.cf_bstatus;
		} 		
		TextView title  = (TextView) findViewById(t);
		TextView status = (TextView) findViewById(s);

		title.setTypeface (anyRemote.protocol.cfTFace);
	    status.setTypeface(anyRemote.protocol.cfTFace);
	    
	    //log("SetFont " +anyRemote.protocol.cfFSize);
	    title.setTextSize (anyRemote.protocol.cfFSize);
	    status.setTextSize(anyRemote.protocol.cfFSize);
	}
    
    
    public void onClick (View v) {  
    	
    	String key = "";
    	
    	switch (v.getId()) {
		
		  case R.id.b1: 
		  case R.id.bb1: 
	    	  key = STR_NUM1;	
			  break;
			  
		  case R.id.b2: 
		  case R.id.bb2: 
	    	  key = STR_NUM2;	
			  break;
			  
		  case R.id.b3: 
		  case R.id.bb3: 
	    	  key = STR_NUM3;	
			  break;
			  
		  case R.id.b4: 
		  case R.id.bb4: 
			  key = STR_NUM4;
			  break;
			  
		  case R.id.b5: 
		  case R.id.bb5: 
			  key = STR_NUM5;
			  break;
			  
		  case R.id.b6: 
		  case R.id.bb6: 
			  key = STR_NUM6;
			  break;
				
		  case R.id.b7: 
		  case R.id.bb7: 
			  key = STR_NUM7;
		      break;
				
		  case R.id.b8: 
			  key = STR_NUM8;
			  break;
				
		  case R.id.b9: 
			  key = STR_NUM9;
			  break;
			  
		  case R.id.b10: 
			  key = STR_STAR;
			  break;
		 
		  case R.id.b0: 
			  key = STR_NUM0;
			  break;
			 
		  case R.id.b11: 
			  key = STR_POUND;
		      break;
      
		  default:
			  log("onClick: Unknown button");
			  return;
		}
    	
    	keyReleased(key);
    }
    
	/*public boolean onTouch(View v, MotionEvent me) {  
    	
    	log("onTouch "+" "+v.getId()+" "+ R.id.b1);
    	
    	int a = me.getAction();
    	//String key = "";
    	int btnIdx = -1;
    	
    	switch (v.getId()) {
		
		  case R.id.b1: 
		  case R.id.bb1: 
	    	  //key = STR_NUM1;
	    	  btnIdx = 0;
			  break;
			  
		  case R.id.b2: 
		  case R.id.bb2: 
	    	  //key = STR_NUM2;
			  btnIdx = 1;
			  break;
			  
		  case R.id.b3: 
		  case R.id.bb3: skin
	    	  //key = STR_NUM3;	
			  btnIdx = 2;
			  break;
			  
		  case R.id.b4: 
		  case R.id.bb4: 
			  //key = STR_NUM4;
			  btnIdx = 3;
			  break;
			  
		  case R.id.b5: 
		  case R.id.bb5: 
			  //key = STR_NUM5;
			  btnIdx = 4;
			  break;
			  
		  case R.id.b6: 
		  case R.id.bb6: 
			  //key = STR_NUM6;
			  btnIdx = 5;
			  break;
				
		  case R.id.b7: 
		  case R.id.bb7: 
			  //key = STR_NUM7;
			  btnIdx = 6;
		      break;
				
		  case R.id.b8: 
			  //key = STR_NUM8;
			  btnIdx = 7;
			  break;
				
		  case R.id.b9: 
			  //key = STR_NUM9;
			  btnIdx = 8;
			  break;
			  
		  case R.id.b10: 
			  //key = STR_STAR;
			  btnIdx = 9;
			  break;
		 
		  case R.id.b0: 
			  //key = STR_NUM0;
			  btnIdx = 10;
			  break;
			 
		  case R.id.b11: 
			  //key = STR_POUND;
			  btnIdx = 11;
		      break;
        
		  default:
			  log("onTouch: Unknown button");
			  return false;
		}
    	
    	if (btnIdx >= 0) {
    	    buttons[btnIdx].setBackgroundColor(
    	    		 a == MotionEvent.ACTION_DOWN ? 
    	    				 anyRemote.protocol.cfFrgr :
    	    				 anyRemote.protocol.cfBkgr);
    	}
    	return false;
    }*/
	
	private String key2str(int keyCode) { 

		switch (keyCode) {
		    case KeyEvent.KEYCODE_1:     return "1";
			case KeyEvent.KEYCODE_2:     return "2";
			case KeyEvent.KEYCODE_3:     return "3";
			case KeyEvent.KEYCODE_4:     return "4";
			case KeyEvent.KEYCODE_5:     return "5";
			case KeyEvent.KEYCODE_6:     return "6";
			case KeyEvent.KEYCODE_7:     return "7";
			case KeyEvent.KEYCODE_8:     return "8";
			case KeyEvent.KEYCODE_9:     return "9";
			case KeyEvent.KEYCODE_STAR:  return "*";
			case KeyEvent.KEYCODE_0:     return "0";
			case KeyEvent.KEYCODE_POUND: return "#";
			case KeyEvent.KEYCODE_MENU: 
			case KeyEvent.KEYCODE_HOME: 
			case KeyEvent.KEYCODE_BACK:   return "";		// do not process them
			case KeyEvent.KEYCODE_VOLUME_UP:   return "VOL+";
			case KeyEvent.KEYCODE_VOLUME_DOWN: return "VOL-";
			case KeyEvent.KEYCODE_DPAD_UP:     
				    return (useJoystick || 
				    		anyRemote.protocol.cfSkin == SK_BOTTOMLINE ? 
				    				 anyRemote.protocol.cfUpEvent : "");   // do not process them if joystick_only param was set
			case KeyEvent.KEYCODE_DPAD_DOWN:   
				    return (useJoystick || 
				    		anyRemote.protocol.cfSkin == SK_BOTTOMLINE ? 
				    				anyRemote.protocol.cfDownEvent : "");
			case KeyEvent.KEYCODE_DPAD_LEFT:   
				    return (useJoystick ? "LEFT" : "");   // do not process them if joystick_only param was set 
			case KeyEvent.KEYCODE_DPAD_RIGHT:  
				    return (useJoystick ? "RIGHT": "");
			case KeyEvent.KEYCODE_DPAD_CENTER: 
				    return (useJoystick ? "FIRE" : "");
			case KeyEvent.KEYCODE_SEARCH:      return "SEARCH";
			default: 
				if (keyCode >= 0 && keyCode < 10) {
					return "K"+String.valueOf(keyCode);
				}
				return String.valueOf(keyCode); 
        }
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) { 
		log("onKeyUp "+keyCode);
		
		String key = key2str(keyCode); 
		if (key.length() > 0) {
            anyRemote.protocol.queueCommand(key, false);
            return true;
		}
        return false;
    }
	
	@Override
	public boolean onKeyDown (int keyCode, KeyEvent event) { 
		
		log("onKeyDown "+keyCode);

		String key = key2str(keyCode);
		if (key.length() > 0) {
            anyRemote.protocol.queueCommand(key, true);
            return true;
		}
	    return false;
    }

	public void keyPressed(int keyCode) {

	}
	
	public void keyPressed(String key) {

	}
	
	public void keyReleased(int keyCode) {
		anyRemote.protocol.queueCommand(keyCode, true);
		anyRemote.protocol.queueCommand(keyCode, false);
 	}
	
	public void keyReleased(String key) {
		anyRemote.protocol.queueCommand(key, true);
		anyRemote.protocol.queueCommand(key, false);
 	}
	    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    commandAction(item.getTitle().toString());
		return true;
	}
	
	@Override
	public void onBackPressed() { 
		commandAction("Exit");
	}
	
	public void commandAction(String command) {
		log("commandAction "+command);
		
        if (command.equals("Exit")) {	
        	doFinish("exit");		  
        } else if (command.equals("Disconnect")) {
        	doFinish("disconnect");	
        } else if (command.equals("Log")) {
        	doFinish("log");	
	    } else {
	        anyRemote.protocol.queueCommand(command);
        }
    }
	
	@Override
	protected void doFinish(String action) {
    	
    	log("doFinish "+action);
    	
	    final Intent intent = new Intent();  
	    intent.putExtra(anyRemote.ACTION, action);
        setResult(RESULT_OK, intent);
        finish();  	
    }
}
