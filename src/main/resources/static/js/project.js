// Vue.component('project-tr', {
//     template: "<tr>" +
//         "<td>content.id</td>" +
//         "<td>content.projectName</td>" +
//         "<td>content.comments</td>" +
//         "</tr>",
//     props: ['content', 'index'],
//     methods: {
//         tclick: function (e) {
//             console.log(e);
//             this.$emit('delete', this.index)
//         }
//     }
// });
// 如果我们通过全局配置了，请求的数据接口 根域名，则 ，在每次单独发起 http 请求的时候，请求的 url 路径，应该以相对路径开头，前面不能带 /  ，否则 不会启用根路径做拼接；
// Vue.http.options.root = 'http://120.79.197.130:8080/';

// 全局启用 emulateJSON 选项:如果Web服务器无法处理编码为application/json的请求，你可以启用emulateJSON选项。
Vue.http.options.emulateJSON = true;
var tableDemo = new Vue({
    el: "#root",
    // components:{
    //     'todo-item':todoItem
    // },
    data: {
        projectId: "",
        name: "",
        comments: "",
        func: true,
        confirm: false,
        todos: []
    },
    created: function () { // 当 vm 实例 的 data 和 methods 初始化完毕后，vm实例会自动执行created 这个生命周期函数
        this.getAllList();
    },
    methods: {
        getAllList: function () {
            var _this = this;
            this.$http.get('/project').then(
                function (data) {
                    _this.todos = data.body.item;
                }
            );
        },
        rowSelect: function (item) {
            this.projectId = item.projectId;
            this.name = item.name;
            this.comments = item.comments;
        },
        newFunc: function () {
            this.projectId = "";
            this.name = "";
            this.comments = "";
            this.func = false;
            this.confirm = true;
        },
        editFunc: function () {
            this.func = false;
            this.confirm = true;
        },
        del: function (index) {
            var temp = "";
            for (var i = 0; i < this.todos.length; i++) {
                if (this.todos[i].projectId == index) {
                    temp = i;
                    break;
                }
            }
            if (!temp) {
                return;
            }
            this.todos.splice(temp, 1);
        },
        submit: function () {
            var _this = this;
            // this.todos.push({
            //     name: this.name,
            //     projectId: this.projectId,
            //     comments: this.comments
            // });
            if (this.projectId) {
                this.$http.put('/project',
                    {
                        projectId: this.projectId,
                        name: this.name,
                        comments: this.comments
                    }, {emulateJSON: true}).then(function (data) {
                        if (data && data.projectId) {
                            console.log(data);
                            this.todos.push(data);
                        }
                    }
                );
                return;
            }
            this.$http.post('/project/p',
                {
                    name: this.name,
                    comments: this.comments
                }).then(function (data) {
                    if (data && data.body && data.body.projectId) {
                        console.log(data);
                        _this.getAllList();
                        // this.todos.push(data);
                    }
                }
            )
        },
        cancelFunc: function () {
            this.func = true;
            this.confirm = false;
        }
    }
});