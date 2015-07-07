package cn.oasistech.agent.server;

import java.net.InetSocketAddress;

import cn.oasistech.agent.AgentContext;
import cn.oasistech.agent.AgentParser;
import cn.oasistech.util.Cfg;
import cn.oasistech.util.ClassUtil;
import cn.oasistech.util.Logger;
import cn.oasistech.util.Server;
import cn.oasistech.util.Address;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class AgentNettyServer implements Server {
    private InetSocketAddress address;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private AgentContext<Channel> agentCtx;
    private Logger logger = new Logger().addPrinter(System.out);
    private AgentHandler<Channel> handler;
    private AgentParser parser;
    
    public boolean start(Address address) {
        if (workerGroup != null) {
            logger.log("server is running on port %d", this.address.getPort());
            return true;
        }
        
        this.parser = ClassUtil.newInstance(Cfg.getParserClassName());
        if (this.parser == null) {
            return false;
        }
        
        this.address = address.toSocketAddress();
        if (this.address == null) {
            return false;
        }
        
        this.bossGroup = new NioEventLoopGroup();
        this.workerGroup = new NioEventLoopGroup();
        this.agentCtx = new AgentContext<Channel>();
        this.handler = new AgentHandler<Channel>(agentCtx, parser);
        
        try {
            ServerBootstrap b = new ServerBootstrap(); 
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class) 
                    .childHandler(new ChannelInitializer<SocketChannel>() { 
                                @Override
                                public void initChannel(SocketChannel ch) throws Exception {
                                    ch.pipeline().addLast(new IdFrameDecoder(), new IdFrameHandler(agentCtx, handler));
                                }
                            }).option(ChannelOption.SO_BACKLOG, 128) 
                    .childOption(ChannelOption.SO_KEEPALIVE, true); 

            b.bind(this.address).sync();
            logger.log("agent server started on port %d", this.address.getPort());
            return true;
        } catch (Exception e) {
            logger.log("start server error:", e);
            
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            return false;
        }
    }
    
    public void stop() {
        if (workerGroup == null) {
            logger.log("server is not running");
            return;
        }
        
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
        
        bossGroup = null;
        workerGroup = null;
        agentCtx = null;
        this.address = null;
        
        logger.log("server stoped on port %d", this.address.getPort());
    }
}
