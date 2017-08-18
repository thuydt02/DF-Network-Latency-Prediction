const F_name = "./output/hello_world.txt";
var F_ = require ("fs");
var stream_F_ = F_.createWriteStream(F_name, {flags:'a'});
stream_F_.write("HELLO WORLD!!!\n");		
stream_F_.end();
