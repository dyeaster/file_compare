var _this = this;
layui.extend({
    dtree: '../lib/layui_ext/dtree/dtree'   // {/}的意思即代表采用自有路径，即不跟随 base 路径
}).use(['dtree', 'tree', 'util', 'table'], function () {
    var $ = layui.jquery;
    var dtree = layui.dtree;
    var DemoTree = dtree.render({
        elem: "#demoTree"
        , data: [] // 使用data加载
        , icon: "2" //修改二级图标样式
        , checkbar: true  //开启复选框
        // url: "../json/case/demoJson.json" // 使用url加载（可与data加载同时存在）
    });
    $.get('/compare/all', function (res) {
        var data = res || [];
        $.each(data, function (index, item) {
            item.id = 'host' + item.id;
            item.title = item.hostIp;
        });
        dtree.reload("demoTree", {
            data: data
        });

    });


    // 绑定节点点击
    dtree.on("node('demoTree')", function (obj) {
        layer.msg(JSON.stringify(obj.param));
        $('.js-dir-result').hide();
        $('.js-file-result').hide();
        var fileInfo = obj.param || {};
        var temp = fileInfo.source.split("/");
        var regex = /\/$/;
        var param = {
            hostIp: fileInfo.hostIp
        };
        if (regex.test(fileInfo.source)) {
            // dir
            $('.js-dir-result').show();
            param.file = temp[temp.length - 2];
            param.type = "dir";
            $.post('/compare/result', param, function (data, status) {
                console.log(data);
                renderDirResult(data);
                dirData = data;
            });
        } else {
            // file
            $('.js-file-result').show();
            param.type = "file";
            param.file = temp[temp.length - 1];
            $.post('/compare/result', param, function (data, status) {
                var result = data.result;
                table.render({
                    elem: '.js-file-table'
                    , id: "fileTable"
                    , initSort: {field: 'sectionName', type: 'asc'}
                    , height: "500"
                    // ,cellMinWidth: 80
                    // , url: '/config/host'
                    // , toolbar: true
                    , data: result
                    // , title: 'Server List'
                    , even: true
                    , size: 'sm'
                    , cols: [[
                        {field: 'sectionName', title: 'Section Name', sort: true}
                        , {field: 'name', title: 'Name'}
                        , {field: 'status', title: 'Status', hide: true}
                        , {field: 'sourceValue', title: 'Source Value', templet: '#targetTpl'}
                        , {field: 'targetValue', title: 'Target Value', templet: '#sourceTpl'}
                    ]]
                    , page: true
                    , limit: 60
                    , limits: [30, 60, 90]

                });
            });
        }
    });
    var table = layui.table;
    var tree = layui.tree
        , layer = layui.layer
        , util = layui.util;
    var dirData = {};
    // tree.render({
    //     elem: '.js-tree'
    //     , data: []
    //     , showCheckbox: true  //是否显示复选框
    //     , id: 'demoId1'
    //     , isJump: true //是否允许点击节点时弹出新窗口跳转
    //     , click: function (obj) {
    //         $('.js-dir-result').hide();
    //         $('.js-file-result').hide();
    //         var fileInfo = obj.data || {};
    //         var temp = fileInfo.source.split("/");
    //         var regex = /\/$/;
    //         var param = {
    //             hostIp: fileInfo.hostIp
    //         };
    //         if (regex.test(fileInfo.source)) {
    //             // dir
    //             $('.js-dir-result').show();
    //             param.file = temp[temp.length - 2];
    //             param.type = "dir";
    //             $.post('/compare/result', param, function (data, status) {
    //                 console.log(data);
    //                 renderDirResult(data);
    //                 dirData = data;
    //             });
    //         } else {
    //             // file
    //             $('.js-file-result').show();
    //             param.type = "file";
    //             param.file = temp[temp.length - 1];
    //             $.post('/compare/result', param, function (data, status) {
    //                 var result = data.result;
    //                 table.render({
    //                     elem: '.js-file-table'
    //                     , id: "fileTable"
    //                     , initSort: {field: 'sectionName', type: 'asc'}
    //                     , height: "500"
    //                     // ,cellMinWidth: 80
    //                     // , url: '/config/host'
    //                     // , toolbar: true
    //                     , data: result
    //                     // , title: 'Server List'
    //                     , even: true
    //                     , size: 'sm'
    //                     , cols: [[
    //                         {field: 'sectionName', title: 'Section Name', sort: true}
    //                         , {field: 'name', title: 'Name'}
    //                         , {field: 'status', title: 'Status', hide: true}
    //                         , {field: 'sourceValue', title: 'Source Value', templet: '#targetTpl'}
    //                         , {field: 'targetValue', title: 'Target Value', templet: '#sourceTpl'}
    //                     ]]
    //                     , page: true
    //                     , limit: 60
    //                     , limits: [30, 60, 90]
    //
    //                 });
    //             });
    //         }
    //     }
    // });
    // $.get('/compare/all', function (res) {
    //     var data = res || [];
    //     $.each(data, function (index, item) {
    //         item.id = 'host' + item.id;
    //         item.title = item.hostIp;
    //     });
    //     tree.reload('demoId1', {
    //         data: data
    //     });
    // });
    // 渲染下部表格
    var cols = [[
        {field: 'fileName', title: 'File Name', fixed: 'left', sort: true}
        , {field: 'filePath', title: 'File Path', sort: true}
        , {field: 'length', title: 'Length'}
        , {field: 'content', title: 'Content', hide: true}
    ]];
    var cols1 = [[
        {field: 'fileName', title: 'File Name', fixed: 'left', sort: true}
        , {field: 'status', title: 'Status', sort: true}
        , {field: 'sourceLength', title: 'Source Length'}
        , {field: 'targetLength', title: 'Target Length'}
    ]];
    // 渲染目录比较表格
    table.render({
        elem: '.js-master-dir'
        , id: "masterTable"
        , initSort: {field: 'fileName', type: 'asc'}
        , height: "300"
        // ,cellMinWidth: 80
        // , url: '/config/host'
        // , toolbar: true
        , data: []
        // , title: 'Server List'
        , even: true
        , cols: cols1
        , page: true
    });
    table.render({
        elem: '.js-slave-dir'
        , id: "slaveTable"
        , initSort: {field: 'filename', type: 'asc'}
        , height: "300"
        // ,cellMinWidth: 80
        // , url: '/config/host'
        // , toolbar: true
        , data: []
        // , title: 'Server List'
        , even: true
        , cols: cols
        , page: true
    });

    // 编辑的方法
    function renderDirResult(data) {
        var master = data.source;
        console.log(master);
        var slave = data.target;
        console.log(slave);
        table.reload('masterTable', {
            data: data.result
        });
        // table.reload('slaveTable', {
        //     data: slave
        // });
    }

    // 请求后台获取树渲染数据
    $.get('/compare/all', function (res) {
        var data = res || [];
        $.each(data, function (index, item) {
            item.id = item.title = item.hostIp
        });
        tree.reload('demoId1', {
            data: data
        });
    });
//按钮事件
    util.event('lay-demo', {
        collectFile: function (othis) {
            var checkedData = tree.getChecked('demoId1') || []; //获取选中节点的数据
            if (checkedData.length === 0) {
                // layer.open({
                //     title: 'Info'
                //     , content: 'Please select a host.'
                //      ,offset: 't'
                // });
                layer.msg("Please select a host.");
                return;
            }
            var host = checkedData[0];
            var param = {
                hostIp: host.hostIp,
                user: host.user
            };
            $.post('/compare/collect', param, function (res, status) {
                var data = res || [];
                $.each(data, function (index, item) {
                    item.id = item.title = item.hostIp;
                    if (item.id === param.hostIp) {
                        item.checked = true;
                        item.spread = true;
                    }
                    item.children = item.fileInfoDtoList || []
                });
                tree.reload('demoId1', {
                    data: data
                });
                layer.msg("Collect file success.", {icon: 6});

            })
        }
        , compareFile: function () {
            var checkedData = tree.getChecked('demoId1') || []; //获取选中节点的数据
            if (checkedData.length === 0) {
                layer.open({
                    title: 'Info'
                    , content: 'Please select a host.'
                    , offset: 't'
                });
                return;
            }
            var host = checkedData[0];
            var param = {
                hostIp: host.hostIp,
                user: host.user
            };
            $.post('/compare/execute', param, function (data, status) {
                layer.msg("File compare complete, please click the node to view the result.", {icon: 6});
                // console.log(xhr);
            });
        }
        , syncConfig: function () {
            var checkedData = tree.getChecked('demoId1') || [];
            if (checkedData.length === 0) {
                layer.open({
                    title: 'Info'
                    , content: 'Please select a host.'
                    , offset: 't'
                });
                return;
            }
            var host = checkedData[0];
            var param = {
                hostIp: host.hostIp,
                user: host.user,
                files: []
            };
            layer.msg("Sync file to target server success.", {icon: 6});
            return;
            $.post('/compare/execute', param, function (data, status) {
                layer.msg("File compare complete, please click the node to view the result.", {icon: 6});
                // console.log(xhr);
            });
        }
        , reload: function () {
        }
    });
});
