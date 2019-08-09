layui.use(['table', 'form'], function () {
    var table = layui.table;
    var form = layui.form;
    var layer = layui.layer;
    form.render();
    var $ = layui.$;
    var projectId = "";
    var hostId = "";
    // 文件的值替换列表 todo
    var valueList = [];
    //第一个实例
    table.render({
        elem: '.js-file-grid'
        , id: "fileTable"
        // ,cellMinWidth: 80
        // , url: '/file'
        , height: 'full - 100'
        // , even: true
        , cols: [[
            {field: 'fileId', title: 'File ID', hide: false}
            , {field: 'source', title: 'Source'}
            , {field: 'target', title: 'Target'}
            , {
                field: 'method', title: 'Compare Method', sort: true,
                templet: function (d) {
                    switch (d.method) {
                        case 1:
                            return 'identical';
                        case 2:
                            return 'exists';
                        case 0:
                            return 'ignore';
                        default:
                            return '';
                    }
                }
            }
            , {
                field: 'type', title: 'File Type', sort: true,
                templet: function (d) {
                    switch (d.type) {
                        case 1:
                            return 'properties';
                        case 2:
                            return 'Ini';
                        case 0:
                            return 'others';
                        default:
                            return '';
                    }
                }
            }
            , {
                field: 'exclude', title: 'Exclude File',
                templet: function (d) {
                    // if (d.exclude) {
                    //     return '<div class="" style="color: #ff0000;">' + d.exclude + '</div>';
                    // }
                    return d.exclude;
                }
            }
            , {field: 'valueMap', title: 'Value Map'}
            // , {title: 'Value Map', toolbar: '#barDemo'}
            // , {field: 'comments', title: 'Remarks'}
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
    //监听行工具事件
    table.on('tool(test)', function (obj) {
        var data = obj.data;
        if (obj.event === 'showDetail') {

        }
    });
    renderProjectSelect();
    // renderHostSelect();

    var curServer = {};
    table.on('row(file-table)', function (obj) {
        // form.val("js-host-form", {});
        var data = obj.data;
        $('.js-confirm-btn').hide();
        $('.js-function-btn').show();
        clearFormData();
        setInputDisplay(data.dirFlag);
        disableFormInput(true);
        // layer.alert(JSON.stringify(data), {
        //     title: '当前行数据：'
        // });
        form.val("js-file-form", data);
        curServer = data;
        //标注选中样式
        obj.tr.addClass('layui-table-click').siblings().removeClass('layui-table-click');
    });
    form.verify({
        filepath: function(value, item){ //value：表单的值、item：表单的DOM对象
            if(!value || value.length === 0){
                return 'The required input should not be null';
            }
            if(!new RegExp("^/").test(value)){
                return 'FilePath should begin with / ';
            }
        }
    });
    form.on('submit(file-submit)', function (data) {
        var param = data.field;
        if (!hostId) {
            layer.msg("Please select host first", {icon: 5, offset: 't'});
            return;
        }
        param.hostId = hostId;
        // var valueMap = getValueMap();
        // return false;
        $.ajax({
            url: "/file",
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify(param),
            dataType: "json",
            success: function (data) {
                if (data && data.fileId) {
                    console.log(data);
                    form.val("js-file-form", data);
                    reloadTable();
                    disableFormInput(false);
                    $('.js-confirm-btn').hide();
                    $('.js-function-btn').show();
                }
            }
        });
        return false;
    });

    // 获取所有的key-value的值
    function getValueMap() {
        var list = [];
        var list2 = [];
        var list3 = [];
        $(".js-key-value-div").each(function () {
            console.log(this);
            console.log($(this));
            var k = this.getElementsByClassName("js-key-input")[0].val();
            var v = this.getElementsByClassName("js-value-input")[0].val();
            list.push(k + "=" + v);
        });
        $(".js-key-input").each(function () {
            var k = $(this).val();
            list2.push(k);
        });
        $(".js-value-input").each(function () {
            var k = $(this).val();
            list3.push(k);
        });
        console.log(list2);
        return list;
    }

    function setInputDisplay(dirFlag) {
        if (dirFlag == 1) {
            $('.js-file-input-div').hide();
            $('.js-dir-input-div').show();
            $('input[name=type]').val('');
            $('input[name=valueMap]').val('');
        } else if (dirFlag == 0) {
            $('.js-dir-input-div').hide();
            $('.js-file-input-div').show();
            $('input[name=exclude]').val('');
        }
    }

    form.on('select(host-sel)', function (data) {
        hostId = data.value;
        if (!hostId) {
            return;
        }
        clearFormData();
        $('input[name=hostId]').val(hostId);
        reloadTable();
        $.ajax({
            url: "/host/4file/" + hostId,
            type: "GET",
            success: function (result) {
                valueList = result || [];
                console.log(valueList);
            }
        });
    });

    form.on('select(dir-select)', function (data) {
        if (!data.value) {
            return;
        }
        // clearFormData();
        setInputDisplay(data.value);
    });
    form.on('select(type-sel)', function (data) {
        if (!data.value) {
            $('input[name=valueMap]').attr('placeholder','');
            return;
        }
        if (data.value == 1) {
            $('input[name=valueMap]').parent().parent().show();
            $('input[name=valueMap]').attr('placeholder','separate with | eg: key1=value1|key2=value2');
        } else if (data.value == 2) {
            $('input[name=valueMap]').parent().parent().show();
            $('input[name=valueMap]').attr('placeholder','separate with | eg: [section1].key1=value1|[section].key2=value2');
        } else {
            $('input[name=valueMap]').parent().parent().hide();
            $('input[name=valueMap]').val('');
        }
    });

    function renderHostSelect() {
        $.ajax({
            url: "/host/project/" + projectId,
            type: "GET",
            dataType: "json",
            success: function (result) {
                var list = result.item || [];    //返回的数据
                var hostDom = document.getElementById("host"); //server为select定义的id
                $('#host').empty();
                if (list.length === 0) {
                    layer.msg("Host has not configured, please add host first.", {offset: 't'});
                    return;
                }
                hostId = list[0].hostId;
                $.each(list, function (index, item) {
                    var option = document.createElement("option");  // 创建添加option属性
                    option.setAttribute("value", item.hostId); // 给option的value添加值
                    option.innerText = item.hostIp;     // 打印option对应的纯文本
                    hostDom.appendChild(option);           //给select添加option子标签
                });
                $('#host').val(hostId);
                form.render("select");
                reloadTable(hostId);
            }
        });
        // 绑定host下拉框选择事件
        // form.on('select(host-sel)', function (data) {
        //     hostId = data.value;
        //     if (!hostId) {
        //         return;
        //     }
        //     clearFormData();
        //     $('input[name=hostId]').val(hostId);
        //     reloadTable();
        //     $.ajax({
        //         url: "/host/4file/" + hostId,
        //         type: "GET",
        //         success: function (result) {
        //             valueList = result || [];
        //             console.log(valueList);
        //         }
        //     });
        // });
    }

    function renderProjectSelect(id) {
        $.ajax({
            url: "/project",
            type: "GET",
            dataType: "json",
            success: function (result) {
                var list = result.item || [];    //返回的数据
                var server = document.getElementById("project"); //server为select定义的id
                if (list.length === 0) {
                    projectId = null;
                    layer.msg("Project has not configured, please add project first.", {offset: 't'});
                    return;
                }
                projectId = list[0].projectId;
                $.each(list, function (index, item) {
                    var option = document.createElement("option");  // 创建添加option属性
                    option.setAttribute("value", item.projectId); // 给option的value添加值
                    option.innerText = item.name;     // 打印option对应的纯文本
                    server.appendChild(option);           //给select添加option子标签
                });
                $('#project').val(projectId);
                form.render("select");
                renderHostSelect();
            }
        });
        // 绑定下拉框选择事件
        form.on('select(project-sel)', function (data) {
            projectId = data.value;
            if (!projectId) {
                return;
            }
            $.ajax({
                url: "/host/project/" + projectId,
                type: "GET",
                dataType: "json",
                success: function (result) {
                    var list = result.item || [];    //返回的数据
                    var hostDom = document.getElementById("host"); //server为select定义的id
                    $('#host').html('');
                    form.render("select");
                    if (list.length === 0) {
                        hostId = null;
                        layer.msg("Host has not configured, please add host first.", {offset: 't'});
                        return;
                    }
                    hostId = list[0].hostId;
                    $.each(list, function (index, item) {
                        var option = document.createElement("option");  // 创建添加option属性
                        option.setAttribute("value", item.hostId); // 给option的value添加值
                        option.innerText = item.hostIp;     // 打印option对应的纯文本
                        hostDom.appendChild(option);           //给select添加option子标签
                    });
                    $('#host').val(hostId);
                    form.render("select");
                    reloadTable(hostId);
                }
            });
        });
    }

    function disableFormInput(flag) {
        $('input[name=fileId]').attr("readonly", flag);
        $('input[name=source]').attr("readonly", flag);
        $('input[name=target]').attr("readonly", flag);
        $('input[name=method]').attr("readonly", flag);
        $('input[name=type]').attr("readonly", flag);
        $('input[name=exclude]').attr("readonly", flag);
        $('input[name=valueMap]').attr("readonly", flag);
        $('input[name=comments]').attr("readonly", flag);
        $('select[name=type]').attr('disabled', flag);
        $('select[name=dirFlag]').attr('disabled', flag);
        $('select[name=method]').attr('disabled', flag);
        form.render('select');
    }

    var active = {
        onAddReplace: function () {
            return;
            // FieldCount++; //text box added increment
            //add input box
            var options = '';
            $.each(valueList, function (index, item) {
                options += '<option value=""' + item.key + '">' + item.key + '</option>\n';
            });
            // console.log(options);
            $('.js-key-value-parent-div').append(
                '<div class="layui-col-md12 js-key-value-div">'
                + '    <div class="layui-col-md6">'
                + '        <div class="layui-form-item">'
                + '            <label class="layui-form-label">Replace key</label>'
                + '            <div class="layui-input-block">'
                + '                <input type="text" name="comments" autocomplete="off" placeholder=""'
                + '                class="layui-input js-key-input">'
                + '            </div>'
                + '        </div>'
                + '    </div>'
                + '    <div class="layui-col-md5">'
                + '        <div class="layui-form-item">'
                + '            <label class="layui-form-label">Replace value</label>'
                + '            <div class="layui-input-block">'
                + '                <select autocomplete="off" placeholder="" class="js-value-input"'
                + '                lay-filter="value-select">'
                + options
                + '                </select>'
                + '            </div>'
                + '        </div>'
                + '    </div>'
                + '    <div class="layui-col-md1 js-delete-value" style="text-align:right;padding-right:10px">'
                + '        <button type="button" class="layui-btn" title="delete replace value" data-type="onAddReplace">\n'
                + '              <i class="layui-icon layui-icon-close" lay-filter="deleteValue"></i></button>'
                + '    </div>'
                + '</div>'
            );
            form.render('select');
        },
        onNewClick: function () {
            clearFormData();
            disableFormInput(false);
            $('.js-confirm-btn').show();
            $('.js-function-btn').hide();
        },
        deleteValue: function () {
            console.log("delete");
        },
        onEditClick: function () {
            disableFormInput(false);
            $('.js-confirm-btn').show();
            $('.js-function-btn').hide();
        },
        onDeleteClick: function () {
            layer.confirm('Are you sure to delete ?', function (index) {
                var param = {};
                param.fileId = $('input[name=fileId]').val();
                $.ajax({
                    url: "/file",
                    type: "DELETE",
                    data: JSON.stringify(param),
                    dataType: "json",
                    contentType: "application/json",
                    success: function (data) {
                        if (data && data.result) {
                            clearFormData();
                            reloadTable();
                            layer.msg("Delete file success.", {icon: 6, offset: 't'});
                        } else {
                            layer.msg("Delete file failed", {icon: 5, offset: 't'});
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
            form.val("js-file-form", curServer);
            curServer = {};
        }
    };
    // 绑定对应事件
    $('.js-layui-btn').on('click', function () {
        var type = $(this).data('type');
        active[type] ? active[type].call(this) : '';
    });


    $(".js-key-value-parent-div").on("click", ".js-delete-value", function () {
        $(this).parent().remove();
        console.log("11111");
    });

    function getFormData() {
        var param = {};
        param.fileId = $('input[name=fileId]').val();
        param.source = $('input[name=source]').val();
        param.target = $('input[name=target]').val();
        param.method = $('input[name=method]').val();
        param.type = $('input[name=type]').val();
        param.exclude = $('input[name=exclude]').val();
        param.comments = $('input[name=comments]').val();
        return param;
    }

    function clearFormData() {
        $('input[name=fileId]').val('');
        $('input[name=fileName]').val('');
        $('input[name=source]').val('');
        $('input[name=target]').val('');
        $('input[name=method]').val('');
        $('input[name=type]').val('');
        $('input[name=exclude]').val('');
        $('input[name=comments]').val('');
        $('input[name=valueMap]').val('');
    }

    function reloadTable() {
        table.reload('fileTable', {
            url: '/file/host/' + hostId
        });
    }

});

