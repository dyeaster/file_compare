//准备视图对象
window.viewObj = {
    tbData: [{
        tempId: new Date().valueOf(),
        sectionName: '',
        key: 'myport',
        attrName: 'port',
        state: 1
    }],
    hostData: [],
    renderSelectOptions: function (data, settings) {
        settings = settings || {};
        var valueField = settings.valueField || 'value',
            textField = settings.textField || 'text',
            selectedValue = settings.selectedValue || "";
        var html = [];
        for (var i = 0, item; i < data.length; i++) {
            item = data[i];
            html.push('<option value="');
            html.push(item[valueField]);
            html.push('"');
            if (selectedValue && item[valueField] == selectedValue) {
                html.push(' selected="selected"');
            }
            html.push('>');
            html.push(item[textField]);
            html.push('</option>');
        }
        return html.join('');
    },
    reloadReplaceTable: function (data) {

    }
};

layui.use(['table', 'form'], function () {
    var table = layui.table;
    var form = layui.form;
    var layer = layui.layer;
    form.render();
    var $ = layui.$;
    var projectId = "";
    var hostId = "";
    var hostData = [];
    var replaceValue = [];


    var tbWidth = $("#tableRes").width();
    var layTableId = "layTable";

    function reloadReplaceTable(data) {
        var temp = table.render({
            elem: '#dataTable',
            id: layTableId,
            data: [],
            width: tbWidth,
            page: true,
            loading: true,
            even: false, //不开启隔行背景
            cols: [[
                // {title: '序号', type: 'numbers'},
                {field: 'sectionName', title: 'Section', edit: 'text'},
                {field: 'key', title: 'Key', edit: 'text'},
                {
                    field: 'attrName', title: 'Attr Name', templet: function (d) {
                        var options = viewObj.renderSelectOptions(hostData, {
                            valueField: "key",
                            textField: "key",
                            selectedValue: d.attrName
                        });
                        return '<a lay-event="type"></a><select name="type" lay-filter="type"><option value="">Please select</option>' + options + '</select>';
                    }
                },
                {
                    field: 'tempId', title: 'Action', width: 70, templet: function (d) {
                        return '<a class="layui-btn layui-btn-xs layui-btn-normal" lay-event="del" lay-id="' + d.tempId + '"><i class="layui-icon layui-icon-delete"></i></a>';
                    }
                }
            ]],
            done: function (res, curr, count) {
                viewObj.tbData = res.data;
            }
        });
        return temp;
    }

    var tableIns = reloadReplaceTable();


    //第一个实例
    table.render({
        elem: '.js-file-grid'
        , id: "fileTable"
        , data: []
        // ,cellMinWidth: 80
        // , url: '/file'
        , height: 'full - 100'
        // , even: true
        , cols: [[
            {field: 'fileId', title: 'File ID', hide: true}
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
            , {field: 'valueMap', title: 'Value Map', hide: true}
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
    //激活事件
    var activeByType = function (type, arg) {
        if (arguments.length === 2) {
            active[type] ? active[type].call(this, arg) : '';
        } else {
            active[type] ? active[type].call(this) : '';
        }
    };
    //注册按钮事件
    $('.layui-btn[data-type]').on('click', function () {
        var type = $(this).data('type');
        activeByType(type);
    });
    //监听select下拉选中事件
    form.on('select(type)', function (data) {
        var elem = data.elem; //得到select原始DOM对象
        $(elem).prev("a[lay-event='type']").trigger("click");
    });

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
                    $.extend(obj.data, {'attrName': selectedVal});
                    activeByType('updateRow', obj.data);	//更新行记录对象
                }
                break;
            case "del":
                // layer.confirm('真的删除行么？', function (index) {
                    obj.del(); //删除对应行（tr）的DOM结构，并更新缓存
                    // layer.close(index);
                    activeByType('removeEmptyTableCache');
                // });
                break;
        }
    });


    renderProjectSelect();
    // renderHostSelect();
    var rootObj = {
        // renderSelectOptions: function (data, settings) {
        //     settings = settings || {};
        //     var valueField = settings.valueField || 'value',
        //         textField = settings.textField || 'text',
        //         selectedValue = settings.selectedValue || "";
        //     var html = [];
        //     for (var i = 0, item; i < data.length; i++) {
        //         item = data[i];
        //         html.push('<option value="');
        //         html.push(item[valueField]);
        //         html.push('"');
        //         if (selectedValue && item[valueField] == selectedValue) {
        //             html.push(' selected="selected"');
        //         }
        //         html.push('>');
        //         html.push(item[textField]);
        //         html.push('</option>');
        //     }
        //     return html.join('');
        // },
        renderProjectSelect: function (id) {

        }
    };

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
                    getHostData();
                }
            });
        });
    }

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
        var temp = data.replaceList || [];
        if (temp.length > 0) {
            $.each(temp, function (index, item) {
                item.tempId = new Date().valueOf();
            });
        }
        // todo
        tableIns.reload({
            data: temp
        });
        //标注选中样式
        obj.tr.addClass('layui-table-click').siblings().removeClass('layui-table-click');
    });
    form.verify({
        filepath: function (value, item) { //value：表单的值、item：表单的DOM对象
            if (!value || value.length === 0) {
                return 'The required input should not be null';
            }
            if (!new RegExp("^/").test(value)) {
                return 'FilePath should begin with / ';
            }
        }
    });
    function validateValueMap(data,type) {
        if(data.length ===0) {
            return true;
        }
        for(var i = 0;i< data.length ;i++) {
            if(!data[i].key || !data[i].attrName || (type ==='2' && !data[i].sectionName)) {
                return false;
            }
        }
        return true;
    }
    form.on('submit(file-submit)', function (data) {
        var param = data.field;
        if (!hostId) {
            layer.msg("Please select host first", {icon: 5, offset: 't'});
            return;
        }
        param.hostId = hostId;

        var oldData = table.cache[layTableId] || [];
        if ((param.type === '1' || param.type === '2') && oldData.length > 0) {
            if(param.type === '2' && !validateValueMap(oldData, param.type)) {
                layer.msg("The section, attr name and attr value should not be null", {offset: 't'});
                return ;
            }
            if(param.type === '1' && !validateValueMap(oldData, param.type)) {
                layer.msg("The attr name and attr value should not be null", {offset: 't'});
                return ;
            }
            param.replaceList = oldData;
        }
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
                    if (param.fileId.length !== 0) {
                        layer.msg("Modify file success .", {icon: 6, offset: 't'});
                        return;
                    }
                    layer.msg("Add file success .", {icon: 6, offset: 't'});
                }
            }
        });
        return false;
    });

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
        getHostData();
    });

    function getHostData() {
        $.ajax({
            url: "/host/4file/" + hostId,
            type: "GET",
            success: function (result) {
                hostData = result || [];
                console.log(hostData);
                tableIns = reloadReplaceTable(hostData);
            }
        });
    }
    form.on('select(dir-select)', function (data) {
        if (!data.value) {
            return;
        }
        // clearFormData();
        setInputDisplay(data.value);
    });
    form.on('select(type-sel)', function (data) {
        if (!data.value) {
            $('input[name=valueMap]').attr('placeholder', '');
            return;
        }
        if (data.value == 1) {
            $('input[name=valueMap]').parent().parent().show();
            $('input[name=valueMap]').attr('placeholder', 'separate with | eg: key1=value1|key2=value2');
        } else if (data.value == 2) {
            $('input[name=valueMap]').parent().parent().show();
            $('input[name=valueMap]').attr('placeholder', 'separate with | eg: [section1].key1=value1|[section].key2=value2');
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
                getHostData();
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
        addRow: function () {	//添加一行
            var oldData = table.cache[layTableId];
            console.log(oldData);
            var newRow = {tempId: new Date().valueOf(), sectionName: null, key: '', attrName: ''};
            oldData.push(newRow);
            tableIns.reload({
                data: oldData
            });
        },
        updateRow: function (obj) {
            var oldData = table.cache[layTableId];
            for (var i = 0, row; i < oldData.length; i++) {
                row = oldData[i];
                if (row.tempId == obj.tempId) {
                    $.extend(oldData[i], obj);
                    return;
                }
            }
            tableIns.reload({
                data: oldData
            });
        },
        removeEmptyTableCache: function () {
            var oldData = table.cache[layTableId];
            for (var i = 0, row; i < oldData.length; i++) {
                row = oldData[i];
                if (!row || !row.tempId) {
                    oldData.splice(i, 1);    //删除一项
                }
                continue;
            }
            tableIns.reload({
                data: oldData
            });
        },
        save: function () {
            var oldData = table.cache[layTableId];
            console.log(oldData);
            for (var i = 0, row; i < oldData.length; i++) {
                row = oldData[i];
                if (!row.type) {
                    layer.msg("检查每一行，请选择分类！", {icon: 5}); //提示
                    return;
                }
            }
            document.getElementById("jsonResult").innerHTML = JSON.stringify(table.cache[layTableId], null, 2);	//使用JSON.stringify() 格式化输出JSON字符串
        },
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

