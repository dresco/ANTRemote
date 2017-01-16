/*
 * Copyright (c) 2016 Jon Escombe (jone@dresco.co.uk)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package uk.co.dresco.antremote;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;

import com.dsi.ant.plugins.antplus.pcc.controls.AntPlusGenericRemoteControlPcc;
import com.dsi.ant.plugins.antplus.pcc.controls.defines.ControlsMode;
import com.dsi.ant.plugins.antplus.pcc.controls.defines.GenericCommandNumber;
import com.dsi.ant.plugins.antplus.pcc.defines.EventFlag;
import com.dsi.ant.plugins.antplus.pcc.defines.RequestAccessResult;
import com.dsi.ant.plugins.antplus.pcc.defines.RequestStatus;
import com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc.IDeviceStateChangeReceiver;
import com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc.IPluginAccessResultReceiver;
import com.dsi.ant.plugins.antplus.pcc.defines.DeviceState;
import com.dsi.ant.plugins.antplus.pccbase.PccReleaseHandle;

import java.util.EnumSet;

public class MainActivity extends AppCompatActivity {

    private AntPlusGenericRemoteControlPcc remotePcc;
    private PccReleaseHandle<AntPlusGenericRemoteControlPcc> remoteReleaseHandle;
    private ProgressBar pb;
    private TextView    tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pb = (ProgressBar) findViewById(R.id.progressBar);
        tv = (TextView) findViewById(R.id.textView);

        ImageButton buttonUp = (ImageButton) findViewById(R.id.buttonUp);
        ImageButton buttonDown = (ImageButton) findViewById(R.id.buttonDown);

        RepeatListener myRepeatListener = new RepeatListener(500, 250, OnClickListener);

        buttonUp.setOnTouchListener(myRepeatListener);
        buttonDown.setOnTouchListener(myRepeatListener);

        connectToRemote();
    }

    protected OnClickListener OnClickListener = new OnClickListener() {
        @Override
        public void onClick(View button) {
            // the code to execute repeatedly
            switch (button.getId()) {
                case R.id.buttonUp:
                    //Toast.makeText(getApplicationContext(), "Up button clicked", Toast.LENGTH_SHORT).show();
                    sendCommand(GenericCommandNumber.MENU_UP);
                    break;

                case R.id.buttonDown:
                    //Toast.makeText(getApplicationContext(), "Down button clicked", Toast.LENGTH_SHORT).show();
                    sendCommand(GenericCommandNumber.MENU_DOWN);
                    break;

                default:
                    break;

            }
        }
    };

    // IPluginAccessResultReceiver
    // Handle the result and ...
    protected IPluginAccessResultReceiver<AntPlusGenericRemoteControlPcc> IPluginAccessResultReceiver =
    new IPluginAccessResultReceiver<AntPlusGenericRemoteControlPcc>() {
        @Override
        public void onResultReceived(AntPlusGenericRemoteControlPcc result, RequestAccessResult resultCode, DeviceState initialDeviceState) {

            // Hide progress bar
            pb.setVisibility(ProgressBar.INVISIBLE);

            switch (resultCode) {

                case SUCCESS:
                    remotePcc = result;
                    Toast.makeText(MainActivity.this, "Channel connected successfully!", Toast.LENGTH_SHORT).show();
                    tv.setText("Connected to device " + result.getAntDeviceNumber());
                    break;

                case SEARCH_TIMEOUT:
                    Toast.makeText(MainActivity.this, "Search timed out, retrying", Toast.LENGTH_SHORT).show();
                    // Attempt another connection
                    connectToRemote();
                    break;

                case CHANNEL_NOT_AVAILABLE:
                    Toast.makeText(MainActivity.this, "Channel Not Available", Toast.LENGTH_SHORT).show();
                    break;

                case BAD_PARAMS:
                    Toast.makeText(MainActivity.this, "BAD_PARAMS", Toast.LENGTH_SHORT).show();
                    break;

                case ADAPTER_NOT_DETECTED:
                    Toast.makeText(MainActivity.this, "ADAPTER_NOT_DETECTED", Toast.LENGTH_SHORT).show();
                    tv.setText("ANT adapter not detected");
                    break;

                case DEPENDENCY_NOT_INSTALLED:
                    Toast.makeText(MainActivity.this, "Dependency not installed", Toast.LENGTH_SHORT).show();
                    tv.setText("Dependency not installed");
                    //goToStore();
                    break;

                case OTHER_FAILURE:
                    Toast.makeText(MainActivity.this, "RequestAccess failed. See logcat for details.", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    // IDeviceStateChangeReceiver
    // Receives state changes and ...
    protected  IDeviceStateChangeReceiver IDeviceStateChangeReceiver =
    new IDeviceStateChangeReceiver()
    {
        @Override
        public void onDeviceStateChange(final DeviceState newDeviceState) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (newDeviceState == DeviceState.CLOSED) {
                        //Toast.makeText(MainActivity.this, "DeviceState CLOSED", Toast.LENGTH_SHORT).show();
                        //tv.setText("DeviceState.CLOSED...");
                    }

                    else if (newDeviceState == DeviceState.DEAD) {
                        //Toast.makeText(MainActivity.this, "DeviceState DEAD", Toast.LENGTH_SHORT).show();
                        //tv.setText("DeviceState.DEAD...");
                        remotePcc = null;
                    }

                    else if (newDeviceState == DeviceState.SEARCHING) {
                        Toast.makeText(MainActivity.this, "Lost connection, retrying", Toast.LENGTH_SHORT).show();
                        //tv.setText("DeviceState.SEARCHING...");
                        // Attempt another connection
                        connectToRemote();
                    }

                    else if (newDeviceState == DeviceState.PROCESSING_REQUEST) {
                        //Toast.makeText(MainActivity.this, "DeviceState PROCESSING_REQUEST", Toast.LENGTH_SHORT).show();
                        //tv.setText("DeviceState.PROCESSING_REQUEST...");
                    }

                    else if (newDeviceState == DeviceState.TRACKING) {
                        //Toast.makeText(MainActivity.this, "DeviceState TRACKING", Toast.LENGTH_SHORT).show();
                        //tv.setText("DeviceState.TRACKING...");
                    }

                    else {
                        Toast.makeText(MainActivity.this, "DeviceState unknown " + newDeviceState, Toast.LENGTH_SHORT).show();
                        //tv.setText("DeviceState.???...");
                    }

                }
            });
        }
    };

    public void buttonClick(View button) {
        // do something useful here
        switch (button.getId()) {

            case R.id.buttonStart:
                //Toast.makeText(getApplicationContext(), "Lap button clicked", Toast.LENGTH_SHORT).show();
                sendCommand(GenericCommandNumber.START);
                break;

            case R.id.buttonStop:
                //Toast.makeText(getApplicationContext(), "Stop button clicked", Toast.LENGTH_SHORT).show();
                sendCommand(GenericCommandNumber.STOP);
                break;

            case R.id.buttonLap:
                //Toast.makeText(getApplicationContext(), "Lap button clicked", Toast.LENGTH_SHORT).show();
                sendCommand(GenericCommandNumber.LAP);
                break;

//            case R.id.buttonUp:
//                //Toast.makeText(getApplicationContext(), "Lap button clicked", Toast.LENGTH_SHORT).show();
//                sendCommand(GenericCommandNumber.MENU_UP);
//                break;
//
//            case R.id.buttonDown:
//                //Toast.makeText(getApplicationContext(), "Lap button clicked", Toast.LENGTH_SHORT).show();
//                sendCommand(GenericCommandNumber.MENU_DOWN);
//                break;

            case R.id.buttonCalibrate:
                //Toast.makeText(getApplicationContext(), "Calibrate button clicked", Toast.LENGTH_SHORT).show();
                sendCommand(GenericCommandNumber.getValueFromInt(0x8000));  // 1st manufacturer custom command
                break;

            default:
                break;
        }
    }

    private void sendCommand(GenericCommandNumber cmd)
    {
        if (remotePcc != null) {
            remotePcc.RequestGenericCommand( new AntPlusGenericRemoteControlPcc.IGenericCommandFinishedReceiver() {

                                                 @Override
                                                 public void onGenericCommandFinished(final long estTimestamp,
                                                                                      final EnumSet<EventFlag> eventFlags, final RequestStatus status)
                                                 {
                                                     runOnUiThread(
                                                             new Runnable()
                                                             {
                                                                 @Override
                                                                 public void run()
                                                                 {
                                                                     switch(status)
                                                                     {
                                                                         case SUCCESS:
                                                                             //Toast.makeText(MainActivity.this, "Request Successfully Sent", Toast.LENGTH_SHORT).show();
                                                                             break;
                                                                         default:
                                                                             //Toast.makeText(MainActivity.this, "Request Failed to be Sent", Toast.LENGTH_SHORT).show();
                                                                             break;
                                                                     }
                                                                 }
                                                             });
                                                 }

                                             },
                    cmd);

        }

    }

    private void connectToRemote()
    {
        EnumSet<ControlsMode> requestModes = EnumSet.of(ControlsMode.GENERIC_MODE);

        //Release the old access if it exists
        if(remoteReleaseHandle != null)
        {
            remoteReleaseHandle.close();
            remoteReleaseHandle = null;
        }

        tv.setText("Searching...");

        // Make the access request, having device set to 0 just connects to the first device found..
        remoteReleaseHandle = AntPlusGenericRemoteControlPcc.requestAccessByDeviceNumber(requestModes,
                                                                                        this,
                                                                                        0, 0,
                                                                                        IPluginAccessResultReceiver,
                                                                                        IDeviceStateChangeReceiver);

        // Show progress bar
        pb.setVisibility(ProgressBar.VISIBLE);

    }

    @Override
    public void onDestroy()
    {
        //Release the old access if it exists
        if(remoteReleaseHandle != null)
        {
            remoteReleaseHandle.close();
            remoteReleaseHandle = null;
        }

        super.onDestroy();
    }


}
