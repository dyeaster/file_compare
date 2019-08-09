// layui.config({
//     base: '../js/'
// });
layui.use(['table', 'form'], function () {
    var table = layui.table;
    var form = layui.form;
    var $ = layui.$;
    // var multiSelect = layui.multiSelect;
    // 异步加载所有host
    // $.ajax({
    //     url: "/host/",
    //     type: "GET",
    //     dataType: "json",
    //     success: function (result) {
    //         var list = result.item || [];    //返回的数据
    //         var hostDom = document.getElementById("host"); //server为select定义的id
    //         // $('#host').empty();
    //         if(list.length=== 0) {
    //             layer.msg("Host has not configured, please add host first.");
    //             return;
    //         }
    //         $.each(list, function (index, item) {
    //             var option = document.createElement("option");  // 创建添加option属性
    //             option.setAttribute("value", item.hostId); // 给option的value添加值
    //             option.innerText = item.hostIp;     // 打印option对应的纯文本
    //             hostDom.appendChild(option);
    //         });
    //         multiSelect.render();
    //     }
    // });
    //第一个实例
    table.render({
        elem: '.js-host-grid'
        , id: "serverTable"
        // ,cellMinWidth: 80
        , url: '/project'
        , height: 'full - 100'
        // , toolbar: true
        , title: 'Server List'
        // , even: true
        , cols: [[
            {field: 'projectId', title: 'Project ID', fixed: 'left'}
            , {field: 'name', title: 'Project Name', sort: true}
            , {field: 'comments', title: 'Remarks'}
            // , {field: 'updateTime', title: 'update time', sort: true}
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
        multiSelect.render();
        curServer = data;
        //标注选中样式
        obj.tr.addClass('layui-table-click').siblings().removeClass('layui-table-click');
    });
    form.on('submit(host-submit)', function (data) {
        var param = data.field;
        // 获取多选下拉的值
        var vals = [],
            texts = [];
        $('select[multiple] option:selected').each(function () {
            vals.push($(this).val());
            texts.push($(this).text());
        });
        var projectId = $('input[name=projectId]').val() || "";
        var type = 'POST';
        if (projectId.length !== 0) {
            type = 'PUT';
        }
        $.ajax({
            url: "/project",
            type: type,
            contentType: "application/json",
            data: JSON.stringify(param),
            dataType: "json",
            success: function (data) {
                if (data && data.projectId) {
                    console.log(data);
                    if (type === 'POST') {
                        layer.msg("Add Project success.", {icon: 6, offset: 't'});
                    } else if (type === 'PUT') {
                        layer.msg("Modify Project success.", {icon: 6, offset: 't'});
                    }
                    table.reload('serverTable', {
                        url: '/project'
                    });
                    form.val("js-host-form", data);
                    disableFormInput(false);
                    $('.js-confirm-btn').hide();
                    $('.js-function-btn').show();
                }
            }
        });
        return false;
    });

    function reloadGrid(id) {
        table.reload('serverTable', {
            url: '/project'
        });
    }


    function disableFormInput(flag) {
        $('input[name=projectId]').attr("readonly", flag);
        $('input[name=name]').attr("readonly", flag);
        $('input[name=comments]').attr("readonly", flag);
    }

    var active = {
        onNewClick: function () {
            clearFormData();
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
                param.projectId = $('input[name=projectId]').val();
                param.name = $('input[name=name]').val();
                param.comments = $('input[name=comments]').val();
                $.ajax({
                    url: "/project",
                    type: "DELETE",
                    data: param,
                    // dataType: "json",
                    // contentType: "application/json",
                    success: function (data) {
                        if (data && data.result) {
                            table.reload('serverTable', {
                                url: '/project'
                            });
                            clearFormData();
                            layer.msg("Delete project success.", {icon: 6, offset: 't'});
                        } else {
                            layer.msg("Delete project failed", {icon: 5, offset: 't'});
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

    function getFormData() {
        var param = {};
        param.projectId = $('input[name=projectId]').val();
        param.name = $('input[name=name]').val();
        param.comments = $('input[name=comments]').val();
        return param;
    }

    function clearFormData() {
        $('input[name=projectId]').val('');
        $('input[name=name]').val('');
        $('input[name=comments]').val('');
    }
});

