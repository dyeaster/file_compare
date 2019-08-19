layui.use(['table', 'element', 'form'], function () {
    var element = layui.element;
    var table = layui.table;
    var form = layui.form;
    var projectId = "";
    var $ = layui.$;
    //第一个实例
    table.render({
        elem: '.js-host-grid'
        , id: "serverTable"
        // ,cellMinWidth: 80
        // , url: '/host'
        , height: 'full - 100'
        // , toolbar: true
        , title: 'Server List'
        // , even: true
        , cols: [[
            {field: 'projectId', title: 'Project Id', hide: true}
            // , {field: 'hostId', title: 'Host ID', fixed: 'left', sort: true}
            , {field: 'hostIp', title: 'IP', sort: true}
            , {field: 'user', title: 'User'}
            , {field: 'port', title: 'Port'}
            , {
                field: 'password', title: 'Password',
                templet: function (d) {
                    return d.password.replace(/.?/g, '*');
                    // return '*************';
                }
            }
        ]]
        , page: true
        , response: {
            statusCode: 200 //重新规定成功的状态码为 200，table 组件默认为 0
        }
        , parseData: function (res) { //将原始数据解析成 table 组件所规定的数据
            console.log(res);
            disableFormInput(true);
            return {
                "code": res.status, //解析接口状态
                "msg": res.message, //解析提示文本
                "count": res.total, //解析数据长度
                "data": res.item //解析数据列表
            };
        }
    });
    var addValue = [];
    var layTableId = "layTable";
    var addTable = table.render({
        elem: '#dataTable'
        , id: layTableId
        , data: addValue
        , page: true
        , loading: true
        , even: true
        , cols: [[
            {field: 'key', title: 'Attr Name', edit: 'text', hide: false}
            // , {field: 'hostId', title: 'Host ID', fixed: 'left', sort: true}
            , {field: 'value', edit: 'text', title: 'Attr Value'}
            , {
                field: 'tempId', title: 'action', width: 70
                , templet: function (d) {
                    return '<a class="layui-btn layui-btn-xs layui-btn-normal" lay-event="del" lay-id="' + d.LAY_TABLE_INDEX + '">' +
                        '<i class="layui-icon layui-icon-delete"></i></a>';
                }
            }
        ]]
        , done: function (res, curr, count) {
            addValue = res.data;
        }
    });
    //监听工具条
    table.on('tool(dataTable)', function (obj) {
        var data = obj.data, event = obj.event, tr = obj.tr; //获得当前行 tr 的DOM对象;
        console.log(data);
        switch (event) {
            case "type":
                //console.log(data);
                var select = tr.find("select[name='type']");
                if (select) {
                    var selectedVal = select.val();
                    if (!selectedVal) {
                        layer.tips("请选择一个分类", select.next('.layui-form-select'), {tips: [3, '#FF5722']}); //吸附提示
                    }
                    console.log(selectedVal);
                    $.extend(obj.data, {'type': selectedVal});
                    activeByType('updateRow', obj.data);	//更新行记录对象
                }
                break;
            case "del":
                // layer.confirm('Are yu sure to delete？', function (index) {
                    obj.del(); //删除对应行（tr）的DOM结构，并更新缓存
                    // layer.close(index);
                    activeByType('removeEmptyTableCache');
                // });
                break;
        }
    });
    $.ajax({
        url: "/project",
        type: "GET",
        dataType: "json",
        success: function (result) {
            var list = result.item || [];    //返回的数据
            var server = document.getElementById("project"); //server为select定义的id
            if (list.length === 0) {
                layer.msg("Project has not configured, please add project first."
                    , {offset: 't'});
                return;
            }
            projectId = list[0].projectId;
            $.each(list, function (index, item) {
                var option = document.createElement("option");  // 创建添加option属性
                option.setAttribute("value", item.projectId); // 给option的value添加值
                option.innerText = item.name;     // 打印option对应的纯文本
                server.appendChild(option);           //给select添加option子标签
                form.render("select");            // 刷性select，显示出数据
            });
            reloadGrid();
        }
    });
    // 绑定下拉框选择事件
    form.on('select(project-sel)', function (data) {
        projectId = data.value;
        if (!projectId) {
            return;
        }

        clearFormData();
        $('input[name=projectId]').val(projectId);
        reloadGrid();
    });
    var curServer = {};
    table.on('row(server-table)', function (obj) {
        // form.val("js-host-form", {});
        var data = obj.data;
        $('.js-confirm-btn').hide();
        $('.js-function-btn').show();
        disableFormInput(true);
        // layer.alert(JSON.stringify(data), {
        //     title: '当前行数据：'
        // });
        clearFormData();
        form.val("js-host-form", data);
        curServer = data;
        var temp = data.hostDetailList || [];
        if (temp.length > 0) {
            $.each(temp, function (index, item) {
                item.tempId = new Date().valueOf();
            });
        }
        addTable.reload({
            data: temp
        });
        //标注选中样式
        obj.tr.addClass('layui-table-click').siblings().removeClass('layui-table-click');
    });
    function validateAdditionValue(data) {
        if(data.length ===0) {
            return true;
        }
        for(var i = 0;i< data.length ;i++) {
            if(!data[i].key || !data[i].value) {
                return false;
            }
        }
        return true;
    }
    form.on('submit(host-submit)', function (data) {
        var oldData = table.cache[layTableId] || [];
        console.log(oldData);
        var param = data.field;
        param.projectId = projectId;
        if (oldData.length > 0 ) {
            if(!validateAdditionValue(oldData)) {
                layer.msg("The attr name and attr value should not be null", {offset: 't'});
                return ;
            }
            param.hostDetailList = oldData;
        }
        var hostId = $('input[name=hostId]').val() || "";
        var type = 'POST';
        if (hostId.length !== 0) {
            type = 'PUT';
        }
        // return false;
        $.ajax({
            url: "/host",
            type: type,
            contentType: "application/json",
            data: JSON.stringify(param),
            dataType: "json",
            success: function (data) {
                if (data && data.code === 0) {
                    console.log(data);
                    reloadGrid();
                    form.val("js-host-form", data);
                    disableFormInput(false);
                    $('.js-confirm-btn').hide();
                    $('.js-function-btn').show();
                    if (hostId.length !== 0) {
                        layer.msg("Modify server success .", {icon: 6, offset: 't'});
                        return;
                    }
                    layer.msg("Add server success .", {icon: 6, offset: 't'});
                } else {
                    layer.msg(data.msg, {icon: 5, offset: 't'});
                }
            }
        });
        return false;
    });

    function reloadGrid() {
        table.reload('serverTable', {
            url: '/host/project/' + projectId
        });
    }

    function getMapFromList(list) {
        var result = {};
        if (list.length === 0) {
            return result;
        }
        $.each(list, function (index, item) {
            result[item.key] = item.value;
        });
        return result;
    }


    function disableFormInput(flag) {
        $('input[name=hostId]').attr("readonly", flag);
        $('input[name=hostIp]').attr("readonly", flag);
        $('input[name=user]').attr("readonly", flag);
        $('input[name=projectId]').attr("readonly", flag);
        $('input[name=password]').attr("readonly", flag);
        $('input[name=port]').attr("readonly", flag);
        $('input[name=comments]').attr("readonly", flag);
    }

    var active = {
        addRow: function () {	//添加一行
            var oldData = table.cache[layTableId];
            console.log(oldData);
            var newRow = {tempId: new Date().valueOf(), key: null, value: ''};
            oldData.push(newRow);
            addTable.reload({
                data: oldData
            });
        },
        updateRow: function (obj) {
            var oldData = table.cache[layTableId];
            console.log(oldData);
            for (var i = 0, row; i < oldData.length; i++) {
                row = oldData[i];
                if (row.tempId == obj.tempId) {
                    $.extend(oldData[i], obj);
                    return;
                }
            }
            addTable.reload({
                data: oldData
            });
        },
        removeEmptyTableCache: function () {
            var oldData = table.cache[layTableId];
            for (var i = 0, row; i < oldData.length; i++) {
                row = oldData[i];
                if (!row || !row.tempId) {
                    oldData.splice(i, 1);    //删除一项
                    break;
                }
            }
            addTable.reload({
                data: oldData
            });
        },
        onNewClick: function () {
            clearFormData();
            // $('input[name=projectId]').val($('input[name=project]'));
            disableFormInput(false);
            $('.js-confirm-btn').show();
            $('.js-function-btn').hide();
        },
        onEditClick: function () {
            disableFormInput(false);
            $('.js-confirm-btn').show();
            $('.js-function-btn').hide();
        },
        onDeleteClick: function () {
            layer.confirm('Are you sure to delete?', function (index) {
                var param = {};
                param.hostId = $('input[name=hostId]').val() || "";
                if (param.hostId.length === 0) {
                    layer.msg("Please select a row.", {icon: 6, offset: 't'});
                    return;
                }
                $.ajax({
                    url: "/host",
                    type: "DELETE",
                    data: param,
                    success: function (data) {
                        if (data && data.result) {
                            reloadGrid();
                            clearFormData();
                            layer.msg("Delete server success.", {icon: 6, offset: 't'});
                        } else {
                            layer.msg("Delete server Failed", {icon: 5, offset: 't'});
                        }
                        layer.close(index);
                    }
                });
            });
        },
        onCancelClick: function () {
            disableFormInput(true);
            $('.js-confirm-btn').hide();
            $('.js-function-btn').show();
            clearFormData();
            form.val("js-host-form", curServer);
            curServer = {};
        }
    };
    $('.js-layui-btn').on('click', function () {
        var type = $(this).data('type');
        active[type] ? active[type].call(this) : '';
    });
    //注册按钮事件
    $('.layui-btn[data-type]').on('click', function () {
        var type = $(this).data('type');
        activeByType(type);
    });
    //激活事件
    var activeByType = function (type, arg) {
        if (arguments.length === 2) {
            active[type] ? active[type].call(this, arg) : '';
        } else {
            active[type] ? active[type].call(this) : '';
        }
    };

    function getFormData() {
        var param = {};
        param.hostId = $('input[name=hostId]').val();
        param.hostIp = $('input[name=hostIp]').val();
        param.user = $('input[name=user]').val();
        param.port = $('input[name=port]').val();
        param.password = $('input[name=password]').val();
        param.projectId = $('input[name=projectId]').val();
        param.comments = $('input[name=comments]').val();
        return param;
    }

    function clearFormData() {
        $('input[name=hostId]').val('');
        $('input[name=hostIp]').val('');
        // $('input[name=projectId]').val('');
        $('input[name=password]').val('');
        $('input[name=user]').val('');
        $('input[name=port]').val('');
        $('input[name=comments]').val('');
        addTable.reload({
            data: []
        });
    }
});

