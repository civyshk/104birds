package net.project104.civyshkbirds;

import android.view.ViewGroup;

import net.project104.swartznetlibrary.NetNode;

import java.lang.ref.WeakReference;
import java.net.InetAddress;

public class NetPlayer extends NetNode {
    private WeakReference<ViewGroup> mWidget;

    public NetPlayer(InetAddress address, int port){
        super(address, port);
    }

}
