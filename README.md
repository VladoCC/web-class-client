# web-class-client

### Usage:
```
val client = TodoClient("http://206.189.36.39", "test", "test")
//CRUD
// create
client.postTodo("todo text")
// read
client.getTodos()
// update
client.updateTodo("updated text", "1")
// delete
client.deleteTodo("1")
```
