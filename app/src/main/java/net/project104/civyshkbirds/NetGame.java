package net.project104.civyshkbirds;

import android.util.Log;

import net.project104.swartznetlibrary.*;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import java.lang.reflect.Constructor;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class NetGame implements NetHandler.ProtocolManager{
    public final String TAG = getClass().getSimpleName();
    static final int COMMAND_TIMEOUT = 10000;//ms
    static final int COMMAND_MAX_TRIES = 1;
    static final String PROTOCOL_VERSION = "0";
    static final int SYNC_PACKETS = 10;
    static final Random rand;
    static {
        rand = new Random();
    }

    static public abstract class Command {
        //REQTEAM & HERETEAM are not used in this game
        enum Type {
            HELLO, SERVER, PLAYERLIST, REQTIME, HERETIME, REQTEAM, HERETEAM, STARTGAME, PROTOCOLERROR, APPERROR
        }

        static BidiMap<Type, Byte> mTypeByteMap;
        static {
            mTypeByteMap = new DualHashBidiMap<>();
            mTypeByteMap.put(Type.HELLO, (byte) 1);
            mTypeByteMap.put(Type.SERVER, (byte) 2);
            mTypeByteMap.put(Type.PLAYERLIST, (byte) 3);
            mTypeByteMap.put(Type.REQTIME, (byte) 4);
            mTypeByteMap.put(Type.HERETIME, (byte) 5);
            mTypeByteMap.put(Type.REQTEAM, (byte) 6);
            mTypeByteMap.put(Type.HERETEAM, (byte) 7);
            mTypeByteMap.put(Type.STARTGAME, (byte) 8);
            mTypeByteMap.put(Type.PROTOCOLERROR, (byte) 9);
            mTypeByteMap.put(Type.APPERROR, (byte) 10);
        }

        static Map<Type, Class> mTypeClassMap;
        static {
            mTypeClassMap = new EnumMap<>(Type.class);
            mTypeClassMap.put(Type.HELLO, CommandHello.class);
            mTypeClassMap.put(Type.SERVER, CommandServer.class);
            mTypeClassMap.put(Type.PLAYERLIST, CommandPlayerlist.class);
            mTypeClassMap.put(Type.REQTIME, CommandReqtime.class);
            mTypeClassMap.put(Type.HERETIME, CommandHeretime.class);
            mTypeClassMap.put(Type.REQTEAM,  CommandReqteam.class);
            mTypeClassMap.put(Type.HERETEAM, CommandHereteam.class);
            mTypeClassMap.put(Type.STARTGAME, CommandStartgame.class);
            mTypeClassMap.put(Type.PROTOCOLERROR, CommandProtocolerror.class);
            mTypeClassMap.put(Type.APPERROR, CommandApperror.class);
        }

        static public byte getByte(Type type) {
            return mTypeByteMap.get(type);
        }

        abstract public Type getType();

        static public Command.Type getType(byte b) {
            return mTypeByteMap.getKey(b);
        }

        static public Class getReflectedClass(byte b){
            return mTypeClassMap.get(getType(b));
        }

        //TODO ver si necesito obtener la clase y el constructor dinámicamente

        static public Constructor getReflectedConstructor(Command.Type t){
            try{
                return mTypeClassMap.get(t).getConstructor(byte[].class);
            }catch(NoSuchMethodException e){
                e.printStackTrace();
                return null;//bug 98ikkm38
            }
        }
    }

    private class CommandHello extends Command{
        private Type mType;
        private byte[] mData;

        public CommandHello(byte[] data) {
            if (data.length != 0) {
                mData = data;
                mType = getType(data[0]);
            }
        }

        public Type getType(){
            return mType;
        }

        public String getPlayerName(){
            return "TODO";
        }

    }

    public interface RoomListener {
        void onPlayerAdd(NetPlayer player);
        void onPlayerRemove(NetPlayer player);
        void onPlayerSwitchRequested(NetPlayer player, int team);
        void onPlayerSwitchConfirmed(NetPlayer player, int team);
        void onPlayerSwitchRejected(NetPlayer player);
        void onStartGame(int seconds);
    }
    private RoomListener mRoomListener;//TODO Use this listener when appropiate

    //TODO implementar sistema de votaciones para:
    //  cambio de equipo de un jugador
    //  expulsar jugador

    private boolean mSelfHosting;
    private NetHandler mNetHandler;
    private JmdHelper mNsd;
    private NetHandler.InitSocketListener mSocketListener;

    private NetPlayer mSelfPlayer, mHostPlayer;
    Set<NetPlayer> mNetPlayers;

    public NetGame(MyApplication app, String playerName, RoomListener roomListener){
        mSelfPlayer = new NetPlayer(null, -1);//todo name, team, host
        mSelfPlayer.setName(playerName);
        mNetPlayers.add(mSelfPlayer);
        mNsd = app.getNsdHelper();
        mNetHandler = app.getNetConnection((NetHandler.ProtocolManager) this);
        Log.d(TAG, "start connection");
        mRoomListener = roomListener;
    }

    public void startServer(ActivityNetRoom act){
        Log.d(TAG, "startServer");
        mSelfHosting = true;
        mSelfPlayer.setHost(true);
        mHostPlayer = mSelfPlayer;
        mNetHandler.startServer(mNsd, new ActivityNetRoom.RoomNsdRegistrationListener(act));
    }

    public void startClient(ActivityNetRoom act){
        Log.d(TAG, "startClient");
        mSelfHosting = false;
        mSelfPlayer.setHost(false);
        //TODO Something goes very slow here
        mNsd.discoverServices(
                new ActivityNetRoom.RoomNsdDiscoveryListener(act),
                new ActivityNetRoom.RoomNsdResolveListener(act),
                mNetHandler.getResolveListener());
    }

    @Override
    public void onPacketReceived(DatagramPacket packet){
        if (packet == null) {
            return;
        }
        if(mSelfHosting){
            Command[] commands = getCommands(packet.getData());
            InetAddress address = packet.getAddress();
            int port = packet.getPort();
            NetPlayer player = getPlayer(address, port);
            for (Command command : commands) {
                switch(command.getType()){
                    case HELLO:
                        String playerName = ((CommandHello) command).getPlayerName();
                        if(playerName == null){
                            //TODO need the sendData(add, prt, Type, String...) method
                            sendData(address, port, Command.Type.PROTOCOLERROR, "Empty player name");
                        }
                        if (player == null) {
                            player = new NetPlayer(address, port);
                        }
                        playerName = assignName(player, playerName);
                        int team = assignTeam(player);
                        sendData(address, port, Command.Type.SERVER, PROTOCOL_VERSION, playerName, String.valueOf(team));
                        sendDataPlayerlist(address, port);
                }


                } else if (getCommandString(CommandType.REQTIME).equals(tokens[0])) {
                    if (player != null) {
                        long time = System.currentTimeMillis();
                        String response = makeResponse(getCommandString(CommandType.TIMEREQ), String.valueOf(time));
                        DatagramPacket sendPacket = makePacket(address, port, response);
                        mSendQueue.add(sendPacket);
                    }
                } else if (getCommandString(CommandType.REQTEAM).equals(tokens[0])) {
                    if (player != null) {
                        String response = makeResponse(getCommandString(CommandType.TEAMREQ));
                        DatagramPacket sendPacket = makePacket(address, port, response);
                        mSendQueue.add(sendPacket);
                    }
                } else if (getCommandString(CommandType.APPERROR).equals(tokens[0])) {
                    if (player != null) {
                        mSendQueue.add(makePacket(address, port, makeResponse(getCommandString(CommandType.PROTOCOLERROR), "Error on your side")));
                        continue;
                    }
                }
            }
        }else{

        }
    }

    @Override
    public void onSocketReady(DatagramSocket socket){
        //assert only for client game
        //TODO
    }
}
