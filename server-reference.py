#!/usr/bin/env python3
"""
Reference TLS tunnel server for TLSTunnelMVP v2.

This intentionally implements only the protocol framing/echo path.
A production gateway must route IPv4 packets (L3) to the Internet and
return packets to the client. Use only on infrastructure you own/control.
"""
import socket, ssl, struct, threading

LISTEN = ("0.0.0.0", 4433)
CERT = "server.crt"
KEY = "server.key"

def recvn(sock, n):
    data=b""
    while len(data)<n:
        chunk=sock.recv(n-len(data))
        if not chunk: return None
        data+=chunk
    return data

def client(conn):
    try:
        hello=conn.recv(32)
        if not hello.startswith(b"TLSTUNNEL-MVP/2"):
            return
        while True:
            h=recvn(conn,4)
            if not h: break
            n=struct.unpack("!I",h)[0]
            if n<=0 or n>32767: break
            packet=recvn(conn,n)
            if packet is None: break
            # Echo for protocol testing. Replace with real L3 forwarding.
            conn.sendall(struct.pack("!I",len(packet))+packet)
    finally:
        conn.close()

ctx=ssl.SSLContext(ssl.PROTOCOL_TLS_SERVER)
ctx.load_cert_chain(CERT,KEY)

with socket.socket() as s:
    s.setsockopt(socket.SOL_SOCKET,socket.SO_REUSEADDR,1)
    s.bind(LISTEN); s.listen(50)
    print(f"Listening on {LISTEN}")
    while True:
        raw,addr=s.accept()
        try:
            conn=ctx.wrap_socket(raw,server_side=True)
            threading.Thread(target=client,args=(conn,),daemon=True).start()
        except Exception:
            raw.close()
