package net.project104.civyshkbirds;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import net.project104.swartznetlibrary.*;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class NetGame implements NetHandler.ProtocolManager{
    public final String TAG = getClass().getSimpleName();
    static final int COMMAND_TIMEOUT = 10000;//ms
    static final int COMMAND_MAX_TRIES = 1;
    static final String PROTOCOL_VERSION = "0";
    static final Random rand;
    static {
        rand = new Random();
    }

    /**
     *                               PROTOCOL
     *
     * CLIENT                                       SERVER                                        .
     * ------                                       ------                                        .
     * HELLO version(s) name(s)          ->         SERVER version(s) clientName(s)               .
     * PLAYERILST size p1add p1port p1name p2... hostPos                                          .
     *
     * REQTIME                           ->         HERETIME serverTime                            .
     * REQTEAM team                      ->         HERETEAM clientTeam                            .
     * .
     * STARTGAME serverTime                           .
     * .
     */

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

    abstract class ReceivedCommand{
        protected Command.Type mType;
        protected boolean mOK;
        public Command.Type getType(){
            return mType;
        };
        public boolean isOK(){
            return mOK;
        }
    };

    //TODO for each command.type
    // DONE: hello, server, playerlist, reqtime, heretime, startgame, protocolerror, apperror
    // NOT: reqteam, hereteam,
    // , implement:
    //1. ENVIAR con función sendCommandSomething(), tanto en
    //  a) Client.handlePacket()
    //  b) this.onPacketReceived() para el Servidor
    //2. RECIBIR con clase CommandSomething(byte[] data)

    private class CommandHello extends ReceivedCommand{
        String mProtocolVersion, mPlayerName;
        public CommandHello(byte[] data) {
            mType = Command.Type.HELLO;
            final ByteArrayInputStream bais = new ByteArrayInputStream(data);
            final DataInputStream dais = new DataInputStream(bais);
            try{
                mProtocolVersion = dais.readUTF();
                mPlayerName = dais.readUTF();
                mOK = true;
            }catch (IOException e){
                mOK = false;
            }
        }

        public String getProtocolVersion(){
            return mProtocolVersion;
        }

        public String getPlayerName(){
            return mPlayerName;
        }

    }

    private class CommandProtocolError extends ReceivedCommand{
        String mMessage;
        public CommandProtocolError(byte[] data) {
            mType = Command.Type.PROTOCOLERROR;
            final ByteArrayInputStream bais = new ByteArrayInputStream(data);
            final DataInputStream dais = new DataInputStream(bais);
            try{
                mMessage = dais.readUTF();
                mOK = true;
            }catch (IOException e){
                mOK = false;
            }
        }
    }

    private class CommandAppError extends ReceivedCommand{
        String mMessage;
        public CommandAppError(byte[] data) {
            mType = Command.Type.APPERROR;
            final ByteArrayInputStream bais = new ByteArrayInputStream(data);
            final DataInputStream dais = new DataInputStream(bais);
            try{
                mMessage = dais.readUTF();
                mOK = true;
            }catch (IOException e){
                mOK = false;
            }
        }
    }

    private class CommandServer extends ReceivedCommand{
        String mProtocolVersion, mPlayerName;//othergames: mPlayerTeam
        public CommandServer(byte[] data){
            mType = Command.Type.SERVER;
            final ByteArrayInputStream bais = new ByteArrayInputStream(data);
            final DataInputStream dais = new DataInputStream(bais);
            try{
                mProtocolVersion = dais.readUTF();
                mPlayerName = dais.readUTF();
                mOK = true;
            }catch (IOException e){
                mOK = false;
            }
        }

        public String getProtocolVersion(){
            return mProtocolVersion;
        }

        public String getPlayerName(){
            return mPlayerName;
        }
        /*
        public int getTeam(){
            return -1;//unused
        }*/
    }

    private class CommandPlayerlist extends ReceivedCommand{
        InetAddress[] mAddresses;
        short[] mPorts;
        String[] mNames;
        short mHostPosition;

        public CommandPlayerlist(byte[] data){
            mType = Command.Type.PLAYERLIST;
            final ByteArrayInputStream bais = new ByteArrayInputStream(data);
            final DataInputStream dais = new DataInputStream(bais);
            try{
                short numberPlayers = dais.readShort();
                mAddresses = new InetAddress[numberPlayers];
                mPorts = new short[numberPlayers];
                mNames = new String[numberPlayers];
                byte[] buffer = new byte[4];
                for(int i=0; i<numberPlayers; i++){
                    int nread = dais.read(buffer, 0, 4);
                    if(nread < 4){//assuming ipv4
                        throw new IOException("Wrong input");
                    }
                    mAddresses[i] = InetAddress.getByAddress(buffer);
                    mPorts[i] = dais.readShort();
                    mNames[i] = dais.readUTF();
                }
                mHostPosition = dais.readShort();
                mOK = true;
            }catch (IOException e){
                mOK = false;
            }
        }
    }

    private class CommandReqTime extends ReceivedCommand{
        public CommandReqTime(byte[] data){
            mType = Command.Type.REQTIME;
            mOK = true;
        }
    }

    private class CommandHereTime extends ReceivedCommand{
        long mTime;
        public CommandHereTime(byte[] data){
            mType = Command.Type.HERETIME;
            final ByteArrayInputStream bais = new ByteArrayInputStream(data);
            final DataInputStream dais = new DataInputStream(bais);
            try{
                mTime = dais.readLong();
                mOK = true;
            }catch (IOException e){
                mOK = false;
            }
        }
    }

    private class CommandStartGame extends ReceivedCommand{
        long mServerTime;
        public CommandStartGame(byte[] data){
            mType = Command.Type.STARTGAME;
            final ByteArrayInputStream bais = new ByteArrayInputStream(data);
            final DataInputStream dais = new DataInputStream(bais);
            try{
                mServerTime = dais.readLong();
                mOK = true;
            }catch (IOException e){
                mOK = false;
            }
        }
    }

    private void sendCommandString(NetPlayer player, Command.Type type, String... argv) {
        if(BuildConfig.DEBUG && !(
                type == Command.Type.HELLO ||
                type == Command.Type.APPERROR ||
                type == Command.Type.PROTOCOLERROR)){
            //TODO bug k9mvmvrmi. TODO eventually update types allowed
        }
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final DataOutputStream daos = new DataOutputStream(baos);
        long packetCount = mSendPacketCount.get(player) + 1;
        mSendPacketCount.put(player, packetCount);
        try {
            daos.writeLong(packetCount);
            daos.writeByte(Command.getByte(type));
            for(String arg : argv){
                daos.writeUTF(arg);
            }
            daos.close();
        }catch(IOException e) {
            e.printStackTrace();
            //error cv8uvu989jj
        }
        final byte[] fullData = baos.toByteArray();
        sendPacket(player.getAddress(), player.getPort(), fullData);

        //TODO assert that packet size < NetHandler.MAX_PACKET_SIZE
    }

    private void sendCommandPlayerlist(NetPlayer player) {
        //format: numPLayers, p1add, p1port, p1name, ... hostPosition
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final DataOutputStream daos = new DataOutputStream(baos);
        long packetCount = mSendPacketCount.get(player) + 1;
        mSendPacketCount.put(player, packetCount);
        try{
            daos.writeLong(packetCount);
            daos.writeByte(Command.getByte(Command.Type.PLAYERLIST));
            daos.writeShort(mNetPlayers.size());
            int i = 0;
            int hostPosition = -1;
            for(NetPlayer p : mNetPlayers){
                daos.write(p.getAddress().getAddress());//i guess that's 4 bytes. not ready for ipv6
                daos.writeShort(p.getPort());
                daos.writeUTF(p.getName());
                if(p.isHost()){
                    hostPosition = i;
                }
                i++;
            }
            if(hostPosition != -1) {
                daos.writeShort(hostPosition);
            }else{
                throw new IndexOutOfBoundsException("No host found in players list");
            }
        }catch(IndexOutOfBoundsException | IOException e){
            e.printStackTrace();
        }

        sendPacket(player.getAddress(), player.getPort(), baos.toByteArray());
    }

    private void sendCommandHereTime(NetPlayer player, long time){
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final DataOutputStream daos = new DataOutputStream(baos);
        long packetCount = mSendPacketCount.get(player) + 1;
        mSendPacketCount.put(player, packetCount);
        try {
            daos.writeLong(packetCount);
            daos.writeByte(Command.getByte(Command.Type.HERETIME));
            daos.writeLong(time);
            daos.close();
        }catch(IOException e) {
            e.printStackTrace();
            //error rf53vvv
        }
        final byte[] fullData = baos.toByteArray();
        sendPacket(player.getAddress(), player.getPort(), fullData);
    }

    private void sendCommandStartGame(long time){
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final DataOutputStream daos = new DataOutputStream(baos);
        try {
            //I send -1 instead of packet number, this way I make one packet for all players
            daos.writeLong(-1);
            daos.writeByte(Command.getByte(Command.Type.STARTGAME));
            daos.writeLong(time);
            daos.close();
        }catch(IOException e) {
            e.printStackTrace();
            //error rf53vvv
        }
        final byte[] fullData = baos.toByteArray();

        for(NetPlayer player : mNetPlayers){
            long packetCount = mSendPacketCount.get(player) + 1;
            mSendPacketCount.put(player, packetCount);
            sendPacket(player.getAddress(), player.getPort(), fullData);
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
    private Client mClient;
    private NetHandler mNetHandler;
    private JmdHelper mNsd;

    private NetPlayer mSelfPlayer, mHostPlayer;
    Set<NetPlayer> mNetPlayers;
    //Recv saves the number of the last packet received from each player
    //Send saves the number of packets sent to each player
    //client should use it; server, perhaps. Just do it, jic.
    //TODO think whether client or server needs these counts
    private Map<NetPlayer, Long> mRecvPacketCount, mSendPacketCount;
    
    public NetGame(MyApplication app, String playerName, RoomListener roomListener){
        mSelfPlayer = new NetPlayer(null, -1);
        mSelfPlayer.setName(playerName);
        mNetPlayers.add(mSelfPlayer);
        mRecvPacketCount = new HashMap<>();
        mSendPacketCount = new HashMap<>();
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
        mClient = new Client();
        //TODO Something goes very slow here
        mNsd.discoverServices(
                new ActivityNetRoom.RoomNsdDiscoveryListener(act),
                new ActivityNetRoom.RoomNsdResolveListener(act),
                mClient.mResolveListener,
                mNetHandler.getResolveListener());
    }

    private class Client {
        private NetTime mNetTime;
        private String mRemoteName;

        JmdHelper.ResolveListener mResolveListener;

        class SentCommand {
            Command.Type mType;
            private boolean mDone;
            private long mTimeSent;
            private int mTries;

            public SentCommand(Command.Type type, long time) {
                mType = type;
                mDone = false;
                mTimeSent = time;
                mTries = 1;
            }

            public boolean isDone() {
                return mDone;
            }

            public void setDone() {
                mDone = true;
            }

            public boolean isTimeout(long timeNow) {
                return (timeNow - mTimeSent) >= COMMAND_TIMEOUT;
            }

            public boolean isTooTried() {
                return mTries >= COMMAND_MAX_TRIES;
            }

            public void updateTries(long timeNow) {
                mTimeSent = timeNow;
                mTries += 1;
            }
        }

        private Set<SentCommand> mSentCommands = new HashSet<>();//I don't expect to send more than one type of command at once

        public Client() {
            mResolveListener = new JmdHelper.ResolveListener() {
                @Override
                public void onServiceResolved(String name, InetAddress address, int port) {
                    mHostPlayer = new NetPlayer(address, port);
                    mHostPlayer.setHost(true);
                    addPlayer(mHostPlayer);
                    mRemoteName = name;
                }

                @Override
                public void onResolveFailed() {

                }
            };
        }

        private boolean isCommandDone(Command.Type type) {
            SentCommand command = getSentCommand(type);
            if (command != null) {
                return command.isDone();
            }
            return false;
        }

        private SentCommand getSentCommand(Command.Type type) {
            for (SentCommand c : mSentCommands) {
                if (c.mType == type) {
                    return c;
                }
            }
            return null;
        }

        public boolean startHello() {
            SentCommand helloCommand = getSentCommand(Command.Type.HELLO);
            if (helloCommand != null) {
                if (helloCommand.isDone()) {
                    mSentCommands.remove(helloCommand);
                } else {
                    return false;
                }
            }
            mSentCommands.add(new SentCommand(Command.Type.HELLO, System.currentTimeMillis()));
            sendCommand(mHostPlayer, Command.Type.HELLO, PROTOCOL_VERSION, mSelfPlayer.getName());
            return true;
        }

        public boolean startSync() {
            SentCommand syncCommand = getSentCommand(Command.Type.REQTIME);
            if (syncCommand != null) {
                if (!syncCommand.isDone()) {
                    return false;
                } else {
                    mSentCommands.remove(syncCommand);
                }
            }
            long timeNow = System.currentTimeMillis();
            mSentCommands.add(new SentCommand(Command.Type.REQTIME, timeNow));
            sendCommandString(mHostPlayer, Command.Type.REQTIME);
            mNetTime = new NetTime();
            mNetTime.addPing(timeNow);
            return true;
        }

        private void continueSync() {
            SentCommand command = getSentCommand(Command.Type.REQTIME);
            if (command == null || command.isDone()) {
                return;
            }
            long timeNow = System.currentTimeMillis();
            if (mNetTime.getPongSize() == NetHandler.SYNC_PACKETS) {
                command.setDone();
                mNetTime.calcLatency(timeNow);
                //now, delay with server is known.
            } else {
                command.updateTries(timeNow);
                sendCommandString(mHostPlayer, Command.Type.REQTIME);
                mNetTime.addPing(timeNow);
            }
        }

        /*
        unused in this game
        public boolean startTeamSwitch() {
            SentCommand teamCommand = getSentCommand(Command.Type.REQTEAM);
            if (teamCommand != null) {
                return false;
            }
            int newTeam = mSelfPlayer.getTeam() == 0 ? 1 : 0;//Benji Price Paralotodo
            mSentCommands.add(new SentCommand(Command.Type.REQTEAM, System.currentTimeMillis()));
            sendCommand(mHostPlayer, Command.Type.REQTEAM, String.valueOf(newTeam));
            return true;
        }
        */

        void handlePacket(@NonNull DatagramPacket packet){
            Log.d(TAG, "RunnableClient got packet");
            ReceivedCommand[] commands = getCommands(packet.getData());
            InetAddress address = packet.getAddress();
            int port = packet.getPort();
            NetPlayer senderPlayer = getPlayer(address, port);
            //we expect only data from host
            if(isHost(senderPlayer)){
                return;
            }
            for (ReceivedCommand command : commands) {
                SentCommand sentCommand;
                switch(command.getType()){
                    case SERVER:
                        //Check: we requested this; it's not done; tokens are OK
                        sentCommand = getSentCommand(Command.Type.HELLO);
                        if (sentCommand == null || sentCommand.isDone()) {
                            continue;
                        }
                        String protocolVersion = ((CommandServer) command).getProtocolVersion();
                        String myName = ((CommandServer) command).getName();
                        //String myTeam = ((CommandServer) command).getTeam();
                        //TODO check 3 fields above. Server might be evil. Version too.
                        sentCommand.setDone();
                        mSelfPlayer.setName(sanitizeName(myName));
                        //mSelfPlayer.setTeam(Integer.parseInt(myTeam));
                        mRoomListener.onPlayerAdd(mSelfPlayer);
                        break;
                    case PLAYERLIST:
                        sentCommand = getSentCommand(Command.Type.HELLO);
                        if (sentCommand == null || !sentCommand.isDone()) {
                            //TODO maybe this PLAYERLIST has arrived before SERVER. Handle it
                            continue;
                        }
                        //TODO loop through players and add to lists (room or game)
                        //TODO check tokens length
                        sentCommand.setDone();
                        /*
                        Set<String> activePlayers = new HashSet<>();
                        for (int team = 0; team < 2; team++) {
                            //          team0: 2  team1: 5         team0: 4  team1: 6
                            for (int p = teamSizePos[team] + 1; p < teamSizePos[team] + 1 + teamSize[team]; p++) {
                                String playerName = tokens[p];
                                NetPlayer netPlayer = getPlayer(playerName);
                                if (netPlayer == null) {
                                    netPlayer = new NetPlayer(null, -1);
                                    netPlayer.setName(sanitizeName(playerName));
                                    netPlayer.setTeam(team);
                                    mNetPlayers.add(netPlayer);
                                    mRoomListener.onPlayerAdd(netPlayer);
                                } else if (netPlayer.getTeam() != team) {
                                    mRoomListener.onPlayerSwitchTeam(netPlayer, team);
                                }
                                activePlayers.add(playerName);
                            }
                        }
                        //remove current players missing from server
                        for (Iterator<NetPlayer> it = mNetPlayers.iterator(); it.hasNext(); ) {
                            NetPlayer netPlayer = it.next();
                            if (!activePlayers.contains(netPlayer.getName())) {
                                if (netPlayer == mPlayer) {
                                    //Server removed us!
                                    startHello();
                                } else {
                                    it.remove();
                                }
                                mRoomListener.onPlayerRemove(netPlayer);
                            }
                        }*/
                        break;
                    case HERETIME:
                        sentCommand = getSentCommand(Command.Type.REQTIME);
                        if (sentCommand == null || sentCommand.isDone()) {
                            continue;
                        }
                        mNetTime.addPong(System.currentTimeMillis());
                        continueSync();//might do calcLatency(), but that's fast enough, surely.
                        break;
                    /*unused in this game
                    case HERETEAM:
                        sentCommand = getSentCommand(Command.Type.REQTEAM);
                        if (sentCommand == null) {
                            continue;
                        }
                        int newTeam = command.getTeam();
                        if (newTeam != mSelfPlayer.getTeam()) {
                            mRoomListener.onPlayerSwitchTeam(mPlayer, newTeam);
                        } else {
                            mRoomListener.onPlayerSwitchRejected(mPlayer);
                        }
                        mSentCommands.remove(sentCommand);
                        break;
                        */
                    case STARTGAME:
                        //TODO. check that sync is done & we have playerlist
                        break;
                }
            }
        }

        private boolean isHost(NetPlayer player){
            return (player == mHostPlayer);
        }

        class RunnableClient implements Runnable {
            //TODO remove this runnable. not needed
            @Override
            public void run() {
                DatagramPacket received = null;
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        received = mRecvQueue.poll(QUEUE_TIMEOUT, TimeUnit.MILLISECONDS);
                        if (received != null) {
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Log.d(TAG, "RunnableClient ends");
            }
        }
    }

    @Override
    public void onPacketReceived(DatagramPacket packet){
        if (packet == null) {
            return;
        }
        ReceivedCommand[] commands = getCommands(packet.getData());
        long packetCount = getPacketCount(packet.getData());
        InetAddress address = packet.getAddress();
        int port = packet.getPort();
        NetPlayer player = getPlayer(address, port);
        if(mSelfHosting){
            if (player == null) {
                player = new NetPlayer(address, port);
                addPlayer(player);
                mRecvPacketCount.put(player, 0L);
            }
            if(packetCount > mRecvPacketCount.get(player)){
                mRecvPacketCount.put(player, packetCount);
            }else{}//TODO handle other cases too?

            for (ReceivedCommand command : commands) {
                switch(command.getType()){
                    case HELLO:
                        String playerName = ((CommandHello) command).getPlayerName();
                        if(playerName == null){
                            sendCommandString(player, Command.Type.PROTOCOLERROR, "Empty player name");
                        }
                        playerName = assignName(player, playerName);
                        //int team = assignTeam(player);
                        sendCommandString(player, Command.Type.SERVER, PROTOCOL_VERSION, playerName);
                        sendCommandPlayerlist(player);
                        break;
                    case REQTIME:
                        long time = System.currentTimeMillis();
                        sendCommandHereTime(player, time);
                        break;
                    /*not needed in this game
                    case REQTEAM:
                        if(player != null){
                            sendCommand(player, Command.Type.REQTEAM);
                        break;
                        */
                    case APPERROR:
                        sendCommandString(player, Command.Type.PROTOCOLERROR, "Error on your side");
                        break;
                }
            }
        }else if(mClient != null){
            if(player == null){
                //omg, TODO panic, unknown node sending packets to us
            }else {
                mClient.handlePacket(packet);
            }
        }
    }

    private long getPacketCount(byte[] data){
        final ByteArrayInputStream bais = new ByteArrayInputStream(data);
        final DataInputStream dais = new DataInputStream(bais);
        long packetCount = -1;
        try{
            packetCount = dais.readLong();
        }catch (IOException e){
        }
        return packetCount;
    }

    private ReceivedCommand[] getCommands(byte[] data){
        //At this moment, every data-packet contains only one command,
        // skipping the packetcount which is probably one (1)
        ReceivedCommand[] commands =  new ReceivedCommand[1];
        commands[0] = getCommand(data, 8, data.length);
        return commands;
        //TODO i will need ot refactor this when more than 1 commands are sent in same packet
        //put all logic in here, loop over the data (while true) and build array of commands
    }

    private ReceivedCommand getCommand(byte[] data, int start, int end) {
        final ByteArrayInputStream bais = new ByteArrayInputStream(data);
        final DataInputStream dais = new DataInputStream(bais);
        try {
            //final long count = dais.readLong();
            dais.skipBytes(start);
            final byte byteType = dais.readByte();
            final byte[] commandData = Arrays.copyOfRange(data, start, data.length);
            //i don't know hoy many bytes to read. TODO tengo que definir la estructura del paquete:
            //pcktcount, numberofcommands, size1, blabla1, size2, bla2, size3, bla3...
            dais.close();
        } catch (IOException e) {
            e.printStackTrace();
            //error z9iskks
        }
        /*TODO steps:
        1. packetCount. It might be an older packet, not useful now. Discard? Yes
        2. Get a constructor based on the type-byte
        3. pass the data
         */

        //2.


    }

    private void sendPacket(InetAddress address, int port, byte[] data) {
        DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
        mNetHandler.sendPacket(packet);
    }

    private void addPlayer(NetPlayer player){
        //As mNetPlayers is a Set, this check is unnecessary
        //if(mNetPlayers.contains(player) == false) {
        mNetPlayers.add(player);
        //}
    }

    private String assignName(NetPlayer player, String name){
        if(BuildConfig.DEBUG &&
                (mNetPlayers.contains(player) && name.equals(player.getName()))){
            throw new AssertionError();
        }
        name = sanitizeName(name);
        NetPlayer conflictingPlayer = getPlayer(name);
        if(conflictingPlayer != null){
            String modifiedName = getModifiedName(name);
            name = assignName(player, modifiedName);
        }else{
            player.setName(name);
        }
        return name;
    }

    private NetPlayer getPlayer(@NonNull InetAddress address, int port){
        for(NetPlayer player : mNetPlayers){
            if(address.equals(player.getAddress()) && player.getPort() == port){
                return player;
            }
        }
        return null;
    }

    private NetPlayer getPlayer(String name){
        for(NetPlayer player : mNetPlayers){
            if(player.getName().equals(name)){
                return player;
            }
        }
        return null;
    }

    private static String sanitizeName(String str){
        //TODO sanitize. Allow predefined charset a-zA-Z0-9? æÆßÖ?
        //or just remove tabs and newlines, which can mean something
        return str;
    }

    private static String getModifiedName(String name){
        //TODO ¿Improve this?
        return name + String.valueOf(rand.nextInt(10));
    }

    private void incrementPacketCount(NetPlayer player) {
        long packetCount = mPacketCount.get(player) + 1;
        mPacketCount.put(player, packetCount);
    }
}
