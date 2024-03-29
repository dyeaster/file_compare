var _this = this;
layui.use(['tree', 'form', 'util', 'table'], function () {
    var $ = layui.jquery;
    var table = layui.table;
    var form = layui.form;
    var tree = layui.tree
        , layer = layui.layer
        , util = layui.util;
    var dirData = {};
    var hostList = [];
    tree.render({
        elem: '.js-tree'
        , data: []
        , showCheckbox: true  //是否显示复选框
        , id: 'demoId1'
        , isJump: true //是否允许点击节点时弹出新窗口跳转
        , click: function (obj) {
            $('.js-dir-result').hide();
            $('.js-file-result').hide();
            $('.js-full-result').hide();

            var fileInfo = obj.data || {};
            // 如果点击的是server节点
            if (!fileInfo.source) {
                return;
            }
            var param = {};
            param.fileId = fileInfo.fileId;
            $.post('/compare/result', param, function (data, status) {
                if (!data || data === {}) {
                    layer.msg("No result found, please compare file first.", {icon: 6, offset: 't'});
                    return;
                }
                if (fileInfo.dirFlag === 1) {
                    $('.js-dir-result').show();
                    console.log(data);
                    renderDirResult(data);
                    dirData = data;
                    return;
                }
                if (fileInfo.type === 0) {
                    layer.msg("the compare result is: " + data.result, {icon: 6, offset: 't'});
                    $('.js-other-result').html('<span style="color:' + (data.result ? 'green' : 'red') + ';">' + data.result + '</span>');
                    $('.js-full-result').show();
                    // data.target = data.target.replace(/\s/g, "<br>");
                    // data.source = data.source.replace(/\s/g, "<br>");
                    // data.target = data.target.join("<br>");
                    // data.source = data.source.join("<br>");
                    // var temp = formatXmlCommon(data.source1);
                    // console.log(temp);
                    var regex = /\.XML/i;
                    if(regex.test(fileInfo.target)) {
                        $('.js-target-full pre').text(formatXmlCommon(data.target1));
                        $('.js-source-full pre').text(formatXmlCommon(data.source1));
                    } else {
                        $('.js-target-full pre').text(data.target1);
                        $('.js-source-full pre').text(data.source1);
                    }
                    return;
                }
                $('.js-file-result').show();
                var result = data.result;
                table.render({
                    elem: '.js-file-table'
                    , id: "fileTable"
                    , initSort: {field: 'sectionName', type: 'asc'}
                    , height: "450"
                    // ,cellMinWidth: 80
                    // , url: '/config/host'
                    // , toolbar: true
                    , data: result
                    // , title: 'Server List'
                    , even: true
                    , size: 'sm'
                    , cols: [[
                        {field: 'sectionName', title: 'Section Name', sort: true, hide: fileInfo.type === 1}
                        , {field: 'method', title: 'Method', sort: true, hide: true}
                        , {field: 'name', title: 'Name'}
                        , {field: 'status', title: 'Status', hide: true}
                        , {
                            field: 'sourceValue', title: 'Source Value',
                            templet: function (d) {
                                if (d.method === 1) {
                                    return d.status === '1' ? d.sourceValue : '<span style="color: red;">' + d.sourceValue + '</span>';
                                } else if (d.method === 2) {
                                    return d.status !== '3' ? d.sourceValue : '<span style="color: red;">' + d.sourceValue + '</span>';
                                }
                                return d.sourceValue;
                            }
                        }
                        , {
                            field: 'targetValue', title: 'Target Value',
                            templet: function (d) {
                                if (d.method === 1) {
                                    return d.status === '1' ? d.targetValue : '<span style="color: red;">' + d.targetValue + '</span>';
                                } else if (d.method === 2) {
                                    return d.status !== '3' ? d.targetValue : '<span style="color: red;">' + d.targetValue + '</span>';
                                }
                                return d.targetValue;
                            }
                        }
                    ]]
                    , page: true
                    , limit: 60
                    , limits: [30, 60, 90]
                });
            });
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
            form.render("select");            // 刷性select，显示出数据

            renderTreeData();
        }
    });
    // 绑定下拉框选择事件
    form.on('select(project-sel)', function (data) {
        projectId = data.value;
        if (!projectId) {
            return;
        }
// 清除树表格数据 todo
//         加载树表格数据 todo
        renderTreeData();
    });

    // 请求后台获取树渲染数据
    function renderTreeData() {
        $('.js-dir-result').hide();
        $('.js-file-result').hide();
        $('.js-full-result').hide();
        $.get('/host/project/' + projectId, function (res) {
            var data = res.item || [];
            $.each(data, function (index, item) {
                item.id = item.hostId;
                item.title = item.hostIp;
            });
            hostList = data;
            tree.reload('demoId1', {
                data: data
            });
        });
    }

    // 渲染下部表格
    var cols = [[
        {field: 'fileName', title: 'File Name', fixed: 'left', sort: true}
        , {field: 'filePath', title: 'File Path', sort: true}
        , {field: 'length', title: 'Length'}
        , {field: 'content', title: 'Content', hide: true}
    ]];
    var cols1 = [[
        {field: 'fileName', title: 'File Name', fixed: 'left', sort: true}
        , {
            field: 'status', title: 'Status',
            templet: function (d) {
                if (d.status !== '1') {
                    return "<span style='color: red;'>different</span>";
                } else {
                    return 'same';
                }
            }
        }
        , {field: 'sourceLength', title: 'Source Length'}
        , {field: 'targetLength', title: 'Target Length'}
    ]];
    // 渲染目录比较表格
    table.render({
        elem: '.js-master-dir'
        , id: "masterTable"
        , initSort: {field: 'fileName', type: 'asc'}
        , height: "450"
        // ,cellMinWidth: 80
        // , url: '/config/host'
        // , toolbar: true
        , data: []
        , size: 'sm'
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
        table.reload('masterTable', {
            data: data.result
        });
        // table.reload('slaveTable', {
        //     data: slave
        // });
    }

    function getFileName(filePath) {
        var regex = /\/$/;
        var temp = filePath.split("/");
        return regex.test(filePath) ? temp[temp.length - 2] : temp[temp.length - 1];
    }

    // param为传入的xml字符串
    function formatXmlCommon(param) {
        var formattedXml = "";
        if (!param) {
            return formattedXml;
        }

        try {
            var heardXml = "";
            var xml = "";
            // 将字符串解析成xml对象
            var doc = $.parseXML(param);

            // 将xml字符串格式话并返回
            if (doc && doc.documentElement && doc.documentElement.outerHTML) {
                xml = this.formatXml(doc.documentElement.outerHTML);
            }

            // 获得原xml文档的头部定义
            var version = doc.xmlVersion || '';
            var encoding = doc.xmlEncoding || '';
            if (param.indexOf('<?xml') != -1) {
                heardXml = "<?xml version=\'" + version + "\' encoding=\'" + encoding + "\'?>\n";
            }
            // 合并xml头部和主体
            formattedXml = heardXml + xml;
        } catch (err) {
            formattedXml = param.replace(/></g, ">\n<");
        }
        return formattedXml;
    }


    function formatXml(xml) {
        var formatted = '';
        // 匹配 ><后接任意字符的 比如 ><id....
        var reg = /(>)(<)(\/*)/g;
        // 往尖括号间插入换行符
        xml = xml.replace(reg, '$1\r\n$2$3');
        var pad = 0;
        // 用换行分隔字符串，然后挨个进行判断，添加缩进
        $.each(xml.split('\r\n'), function (index, node) {
            // 本节点的相对缩进量 相对上一个节点
            var indent = 0;
            // 匹配类似 gsgs</zsmart> 这种，即包含元素的节点
            if (node.match(/.+<\/\w[^>]*>$/)) {
                // 起始元素，不用缩进
                indent = 0;
                // 匹配以</开头的字符，比如 </zsmart
            } else if (node.match(/^<\/\w/)) {
                // 非起始节点，该节点为结束标签，缩进量减1
                if (pad != 0) {
                    pad -= 1;
                }
                // 匹配以<开头，然后接任意字符，中间包含>并以任一字符结尾，然后接任意字符 比如 <kjl>lkjlj</hjgj>
            } else if (node.match(/^<\w[^>]*[^\/]>.*$/)) {
                // 子元素，缩进量为1
                indent = 1;
            } else {
                indent = 0;
            }

            // 获得该元素的缩进量
            var padding = '';
            for (var i = 0; i < pad; i++) {
                padding += ' ';
            }

            // 组合缩进量以及内容加换行
            formatted += padding + node + '\r\n';
            // 获取本节点的缩进量，传入下一个节点
            pad += indent;
        });
        return formatted;
    }


//按钮事件
    util.event('lay-demo', {
        collectFile: function (othis) {
            var checkedData = tree.getChecked('demoId1') || []; //获取选中节点的数据
            if (checkedData.length === 0) {
                // layer.open({
                //     title: 'Info'
                //     , content: 'Please select a host.'
                //     ,offset: 't'
                // });
                layer.msg("Please select a host.", {offset: 't'});
                return;
            }
            var host = checkedData[0];
            var param = {
                hostId: host.hostId
            };
            $.post('/compare/collect', param, function (res, status) {
                console.log(res.result);
                var data = res.files || [];
                // 处理file的数据，加上title，id
                $.each(data, function (index, item) {
                    item.id = item.fileId;
                    item.title = getFileName(item.source);
                    // item.hostIp = host.hostIp;
                    // item.user = host.user;
                });
                $.each(hostList, function (index, item) {
                    if (item.hostId === host.hostId) {
                        item.children = data || [];
                        item.checked = true;
                        item.spread = true;
                    }
                });
                tree.reload('demoId1', {
                    data: hostList
                });
                layer.msg("Collect file success.", {icon: 6, offset: 't'});
            });
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
            var files = host.children || [];
            if (files.length === 0) {
                layer.msg("Please select file.");
                return;
            }
            var fileIds = [];
            $.each(files, function (index, item) {
                fileIds.push(item.fileId);
            });
            host.fileInfoList = host.children;
            var param = {
                hostId: host.hostId,
                fileIds: fileIds
            };
            $.post('/compare/execute', param, function (data, status) {
                console.log(data);
                layer.msg("File compare complete, please click the node to view the result.", {icon: 6, offset: 't'});
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
            var fileId = $.map(host.children, function (item) {
                return item.id;
            });
            var param = {
                hostIp: host.hostIp,
                user: host.user,
                fileId: fileId
            };
            $.post('/compare/sync', param, function (data, status) {
                console.log(data);
                layer.msg("Sync file to target server success.", {icon: 6, offset: 't'});
            });
        }
        , reload: function () {
        }
    });
});
