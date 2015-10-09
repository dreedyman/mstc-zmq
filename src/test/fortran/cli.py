import zmq

if __name__ == "__main__":
    context = zmq.Context()
    socket = context.socket(zmq.REQ)
    socket.connect('tcp://localhost:5555')

    socket.send('isprime', zmq.SNDMORE)
    socket.send_json({"value": 7919})
    print socket.recv_json()['result']

    socket.send('average', zmq.SNDMORE)
    socket.send_json({"values": [1.2, 3.4, 5.6, 7.8]})
    print socket.recv_json()['result']

    socket.close()
    context.term()
