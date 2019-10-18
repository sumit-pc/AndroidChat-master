const express = require('express'),
http = require('http'),
app = express(),
server = http.createServer(app),
io = require('socket.io').listen(server);
app.get('/', (req, res) => {

res.send('Grievance Management System app is running on port 56455')
});
io.on('connection', (socket) => {

//console.log('user connected')

socket.on('join', function(userNickname) {

        console.log(userNickname +" : has joined the chat "  );
        socket.broadcast.emit('countChange',io.engine.clientsCount);
        socket.broadcast.emit('userjoinedthechat',userNickname +" : has joined the chat ");
    });


socket.on('messagedetection', (senderNickname,messageContent) => {
       
       //log the message in console 

       console.log(senderNickname+" :" +messageContent)
        //create a message object 
       let  message = {"message":messageContent, "senderNickname":senderNickname}
          // send the message to the client side  
       io.emit('message', message );
       socket.broadcast.emit('countChange',io.engine.clientsCount);
     
      });

socket.on('requireCount', function() {
        //console.log(onlineUser +" :requireCount No of User ");
        onlineUser = io.engine.clientsCount;
        socket.broadcast.emit('countChange',io.engine.clientsCount);
    });
      
  
 socket.on('disconnect', function() {
    console.log( ' user has left ')
    socket.broadcast.emit('countChange',io.engine.clientsCount);
    socket.broadcast.emit("userdisconnect"," user has left ") 

});


socket.on('killApp', function(Nickname) {

    console.log(Nickname +" : has left the chat "  );
    socket.broadcast.emit('countChange',io.engine.clientsCount);
    socket.broadcast.emit('killAppuser',Nickname +" : has left the chat ");
});



});


app.post('/get/ip/address', function (req, res) {
    // need access to IP address here

})


server.listen(3000,()=>{

console.log('A chat Application is running on 3000');

});
