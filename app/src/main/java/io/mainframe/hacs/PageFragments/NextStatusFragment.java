package io.mainframe.hacs.PageFragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.EnumSet;

import io.mainframe.hacs.R;
import io.mainframe.hacs.common.Constants;
import io.mainframe.hacs.components.DoorButtons;
import io.mainframe.hacs.main.Status;
import io.mainframe.hacs.mqtt.MqttConnector;
import io.mainframe.hacs.mqtt.MqttStatusListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class NextStatusFragment extends BasePageFragment implements MqttStatusListener {

    private DoorButtons doorButtons;
    // if the mqtt password is not set
    private boolean readOnlyMode = false;

    public NextStatusFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_next_status, container, false);
        doorButtons = (DoorButtons) view.findViewById(R.id.nextStatus_doorButtons);
        doorButtons.setOnButtonClickListener(new DoorButtons.OnButtonClickListener() {
            @Override
            public void onClick(DoorButtons.DoorButton doorButton, View view) {
                getInteraction().getMqttConnector()
                        .send(Constants.MQTT_TOPIC_STATUS_NEXT, doorButton.getStatus().getMqttValue());
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        final MqttConnector mqtt = getInteraction().getMqttConnector();
        mqtt.addListener(this, EnumSet.of(Topic.STATUS_NEXT));

        setStatusText(mqtt.getLastValue(Topic.STATUS_NEXT, Status.class));

        if (!mqtt.isPasswordSet()) {
            readOnlyMode = true;
            doorButtons.setEnabled(false);
            getView().findViewById(R.id.nextStatus_clearStatus).setEnabled(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        getInteraction().getMqttConnector().removeAllListener(this);
    }

    private void setStatusText(Status status) {
        TextView text = (TextView) getView().findViewById(R.id.nextStatus_status);
        text.setText(status == null ? getString(R.string.unknown) : status.getUiValue());

        if (!readOnlyMode) {
            doorButtons.setEnabled(status != null);
        }
    }

    @Override
    public int getTitleRes() {
        return R.string.nav_status_next;
    }

    /* callback */

    @Override
    public void onNewMsg(Topic topic, Object msg) {
        if (topic != Topic.STATUS_NEXT) {
            return;
        }
        setStatusText((Status) msg);
    }

    @Override
    public void onMqttConnected() {
        setStatusText(getInteraction().getMqttConnector().getLastValue(Topic.STATUS_NEXT, Status.class));
    }

    @Override
    public void onMqttConnectionLost() {
        setStatusText(null);
        doorButtons.setEnabled(false);
    }
}
