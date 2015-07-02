package cn.oasistech.agent.server;

import cn.oasistech.agent.AgentContext;
import cn.oasistech.agent.AgentParser;
import cn.oasistech.util.Logger;
import cn.oasistech.util.Server;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class AgentNettyServer implements Server {
    private int port;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private AgentContext<Channel> agentCtx;
    private Logger logger = new Logger().addPrinter(System.out);
    private AgentHandler<Channel> handler;
    private AgentParser parser;
    
    public AgentNettyServer(AgentParser parser) {
        this.parser = parser;
    }
    
    public void start(int port) {
        if (workerGroup != null) {
            logger.log("server is running on port %d", port);
        }
        
        this.port = port;
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

            b.bind(port).sync();
            logger.log("agent server started on port %d", port);
        } catch (Exception e) {
            logger.log("start server error:", e);
            
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
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
        port = 0;
        
        logger.log("server stoped on port %d", port);
    }
}
