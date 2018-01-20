package io.mainframe.hacs.PageFragments;

import android.content.Context;
import android.support.v4.app.Fragment;

import io.mainframe.hacs.main.NetworkStatus;
import io.mainframe.hacs.mqtt.MqttConnector;

/**
 * Created by holger on 06.10.17.
 */

public abstract class BasePageFragment extends Fragment {


    private BasePageFragmentInteractionListener listener;

    public abstract int getTitleRes();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof BasePageFragmentInteractionListener) {
            listener = (BasePageFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement BasePageFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    protected BasePageFragmentInteractionListener getInteraction() {
        return this.listener;
    }

    public interface BasePageFragmentInteractionListener {
        NetworkStatus getNetworkStatus();

        MqttConnector getMqttConnector();

        void sendSshCommand(String command);

        void navigateToPage(Class<? extends BasePageFragment> target);
    }

}