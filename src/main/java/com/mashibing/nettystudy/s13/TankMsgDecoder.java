package com.mashibing.nettystudy.s13;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class TankMsgDecoder extends ByteToMessageDecoder {

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		//д����������8���ֽ�
		//�����TCP�����ճ��������
		if(in.readableBytes()<8) return;
		//in.markReaderIndex();
		
		//��Ϊ��д��x����д��y�����Զ���ʱ���ȶ�����x���ٶ�����y
		int x = in.readInt();
		int y = in.readInt();
		
		//����Ǹ���Ϣ���������Ķ���ȫ��װ��list��Ϳ�����
		out.add(new TankMsg(x, y));
	}
}
