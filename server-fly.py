#!/usr/bin/env python3

import socket
import struct
import threading

HOST = "0.0.0.0"
PORT = 4433

PROTOCOL = b"TLSTUNNEL-MVP/2"
MAX_PACKET = 32767


def recvn(sock, size):
    data = b""

    while len(data) < size:
        chunk = sock.recv(size - len(data))

        if not chunk:
            return None

        data += chunk

    return data


def handle_client(conn, addr):
    print(f"Cliente conectado: {addr}")

    try:
        hello = conn.recv(len(PROTOCOL))

        if hello != PROTOCOL:
            print(f"Protocolo inválido: {addr}")
            return

        print(f"Protocolo aceito: {addr}")

        while True:

            header = recvn(conn, 4)

            if header is None:
                break

            size = struct.unpack("!I", header)[0]

            if size <= 0 or size > MAX_PACKET:
                print(f"Pacote inválido: {size}")
                break

            packet = recvn(conn, size)

            if packet is None:
                break

            # MVP: ecoa o pacote para testar a comunicação.
            response = (
                struct.pack("!I", len(packet))
                + packet
            )

            conn.sendall(response)

    except Exception as error:
        print(f"Erro com {addr}: {error}")

    finally:
        conn.close()
        print(f"Cliente desconectado: {addr}")


def main():

    server = socket.socket(
        socket.AF_INET,
        socket.SOCK_STREAM
    )

    server.setsockopt(
        socket.SOL_SOCKET,
        socket.SO_REUSEADDR,
        1
    )

    server.bind((HOST, PORT))
    server.listen(50)

    print(
        f"TLS Tunnel backend ouvindo em "
        f"{HOST}:{PORT}"
    )

    while True:

        conn, addr = server.accept()

        thread = threading.Thread(
            target=handle_client,
            args=(conn, addr),
            daemon=True
        )

        thread.start()


if __name__ == "__main__":
    main()
