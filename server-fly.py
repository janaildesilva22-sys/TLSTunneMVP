#!/usr/bin/env python3

import socket
import struct
import threading

HOST = "0.0.0.0"
PORT = 4433


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
        # SOCKS5 handshake
        header = recvn(conn, 2)

        if not header or header[0] != 5:
            return

        methods_count = header[1]
        methods = recvn(conn, methods_count)

        if methods is None:
            return

        # Sem autenticação
        conn.sendall(b"\x05\x00")

        # SOCKS5 request
        request = recvn(conn, 4)

        if not request or request[0] != 5:
            return

        command = request[1]
        address_type = request[3]

        if command != 1:
            conn.sendall(
                b"\x05\x07\x00\x01"
                b"\x00\x00\x00\x00\x00\x00"
            )
            return

        # IPv4
        if address_type == 1:
            raw_address = recvn(conn, 4)
            destination_host = socket.inet_ntoa(raw_address)

        # Domain name
        elif address_type == 3:
            length = recvn(conn, 1)[0]
            raw_domain = recvn(conn, length)
            destination_host = raw_domain.decode()

        # IPv6
        elif address_type == 4:
            raw_address = recvn(conn, 16)
            destination_host = socket.inet_ntop(
                socket.AF_INET6,
                raw_address
            )

        else:
            return

        raw_port = recvn(conn, 2)

        if raw_port is None:
            return

        destination_port = struct.unpack(
            "!H",
            raw_port
        )[0]

        print(
            f"Conectando em "
            f"{destination_host}:{destination_port}"
        )

        remote = socket.create_connection(
            (destination_host, destination_port),
            timeout=15
        )

        # SOCKS5 success
        conn.sendall(
            b"\x05\x00\x00\x01"
            b"\x00\x00\x00\x00\x00\x00"
        )

        relay(conn, remote)

    except Exception as error:
        print(f"Erro com {addr}: {error}")

    finally:
        try:
            conn.close()
        except Exception:
            pass

        print(f"Cliente desconectado: {addr}")


def relay(client, remote):
    sockets = [client, remote]

    while True:
        readable, _, _ = select.select(
            sockets,
            [],
            [],
            60
        )

        if not readable:
            break

        for sock in readable:
            data = sock.recv(32768)

            if not data:
                return

            if sock is client:
                remote.sendall(data)
            else:
                client.sendall(data)


def main():
    global select

    import select

    server = socket.socket(
        socket.AF_INET,
        socket.SOCK_STREAM
    )

    server.setsockopt(
        socket.SOL_SOCKET,
        socket.SO_REUSEADDR,
        1
    )

    server.bind(
        (HOST, PORT)
    )

    server.listen(100)

    print(
        f"SOCKS5 server ouvindo "
        f"em {HOST}:{PORT}"
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
