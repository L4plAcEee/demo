<template>
    <div>
        <el-container style="height: 100vh;">
            <el-header class="system-header">学生信息管理系统</el-header>
            <el-container>
                <el-aside width="230px" class="sidebar">
                    <el-menu :default-openeds="['1', '3']">
                        <el-submenu index="1">
                            <template slot="title"><i class="el-icon-message"></i>导航</template>

                            <el-menu-item index="1-1">
                                <router-link to="/dept">学生信息（简洁）</router-link>
                            </el-menu-item>
                            <el-menu-item index="1-2">
                                <router-link to="/emp">学生信息</router-link>
                            </el-menu-item>
                            <el-menu-item index="1-3">
                                <router-link to="/ClC">学生课程信息</router-link>
                            </el-menu-item>
                        </el-submenu>
                    </el-menu>
                </el-aside>


                <el-main>
                    <!--from表单-->
                    <el-form :inline="true" :model="searchForm" class="demo-form-inline">
                        <el-form-item label="姓名">
                            <el-input v-model="searchForm.name" placeholder="姓名"></el-input>
                        </el-form-item>
                        <el-form-item label="性别">
                            <el-select v-model="searchForm.gender" placeholder="性别">
                                <el-option label="男" value="1"></el-option>
                                <el-option label="女" value="2"></el-option>
                            </el-select>
                        </el-form-item>
                        <el-form-item>
                            <el-button type="primary" @click="onSubmit">查询</el-button>
                        </el-form-item>
                        <el-form-item>
                            <el-button type="primary" @click="returnLog">退出登录</el-button>
                        </el-form-item>

                    </el-form>


                    <el-button type="primary" @click="dialogFormVisibleInsert = true">添加学生</el-button>
                    <el-dialog title="学生信息" :visible.sync="dialogFormVisibleInsert">
                        <el-form :model="formInsert">
                            <el-form-item label="姓名" :label-width="formLabelWidth">
                                <el-input v-model="formInsert.name" autocomplete="off"></el-input>
                            </el-form-item>
                            <el-form-item label="密码" :label-width="formLabelWidth">
                                <el-input v-model="formInsert.password" autocomplete="off"></el-input>
                            </el-form-item>
                            <el-form-item label="性别" :label-width="formLabelWidth">
                                <el-select v-model="formInsert.gender" placeholder="请选择性别">
                                    <el-option label="男" value="1"></el-option>
                                    <el-option label="女" value="2"></el-option>
                                </el-select>
                            </el-form-item>
                            <el-form-item label="住址" :label-width="formLabelWidth">
                                <el-input v-model="formInsert.address" autocomplete="off"></el-input>
                            </el-form-item>
                            <el-form-item label="邮箱" :label-width="formLabelWidth">
                                <el-input v-model="formInsert.email" autocomplete="off"></el-input>
                            </el-form-item>
                            <el-upload class="avatar-uploader" action="http://localhost:8044/Upload"
                                name="image" 
                                :show-file-list="false" 
                                :on-success="handleAvatarSuccess_Insert"
                                :before-upload="beforeAvatarUpload_Insert"
                                :headers="headers">
                                <img v-if="imageUrl" :src="imageUrl" class="avatar">
                                <i v-else class="el-icon-plus avatar-uploader-icon"></i>
                            </el-upload>
                        </el-form>
                        <div slot="footer" class="dialog-footer">
                            <el-button @click="dialogFormVisibleInsert = false">取 消</el-button>
                            <el-button type="primary"
                                @click="dialogFormVisibleInsert = false, onSubmitInsert(formInsert)">确定</el-button>
                        </div>
                    </el-dialog>


                    <!-- 表格--->
                    <el-table :data="tableData" border>
                        <el-table-column prop="id" label="id" width="200">
                            <template slot-scope="scope">
                                {{ scope.row.id }}
                            </template>
                        </el-table-column>
                        <el-table-column prop="image" label="头像" width="200">
                            <template slot-scope="scope">
                            <img v-if="image" 
                            :src="scope.row.image"
                            class="avatar"
                            alt="图片加载失败">
                            
                        </template>
                        </el-table-column>
                        <el-table-column prop="name" label="姓名" width="200">
                            <template slot-scope="scope">
                                {{ scope.row.name == null ? "无" : scope.row.name }}
                            </template>
                        </el-table-column>
                        <el-table-column prop="password" label="密码" width="200">
                            <template slot-scope="scope">
                                {{ scope.row.password == null ? id : "*******" }}
                            </template>
                        </el-table-column>
                        <el-table-column label="性别" width="200">
                            <template slot-scope="scope"><!--这是个插槽，但是还是具体的看不懂。。。-->
                                {{ scope.row.gender == 1 ? '男' : '女' }}
                            </template>
                        </el-table-column>
                        <el-table-column prop="birthday" label="生日" width="200">
                            <template slot-scope="scope">
                                {{ scope.row.birthday == null ? "无" : scope.row.birthday }}
                            </template>
                        </el-table-column>
                        <el-table-column prop="address" label="家庭地址" width="200">
                            <template slot-scope="scope">
                                {{ scope.row.address == null ? "无" : scope.row.address }}
                            </template>
                        </el-table-column>
                        <el-table-column prop="email" label="邮箱" width="200">
                            <template slot-scope="scope">
                                {{ scope.row.email == null ? "无" : scope.row.email }}
                            </template>
                        </el-table-column>

                        <el-table-column label="操作">
                            <el-row slot-scope="scope">
                                <el-button 
                                type="primary" 
                                size="mini"
                                @click="dialogFormVisible = true, 
                                uploadGetId(scope.row.id)">编辑
                                </el-button>
                                    
                                <el-dialog title="资料编辑" :visible.sync="dialogFormVisible">
                                    <el-form :model="form">
                                        <el-form-item label="姓名" :label-width="formLabelWidth">
                                            <el-input v-model="form.name" autocomplete="off"></el-input>
                                        </el-form-item>
                                        <el-form-item label="性别" :label-width="formLabelWidth">
                                            <el-select v-model="form.gender" placeholder="请选择性别">
                                                <el-option label="男" value="1"></el-option>
                                                <el-option label="女" value="2"></el-option>
                                            </el-select>
                                        </el-form-item>
                                        <el-form-item label="住址" :label-width="formLabelWidth">
                                            <el-input v-model="form.address" autocomplete="off"></el-input>
                                        </el-form-item>
                                        <el-form-item label="邮箱" :label-width="formLabelWidth">
                                            <el-input v-model="form.email" autocomplete="off"></el-input>
                                        </el-form-item>
                                        <el-upload class="avatar-uploader" action="http://localhost:8044/Upload"
                                            name="image" 
                                            :show-file-list="false" 
                                            :on-success="handleAvatarSuccess_Update"
                                            :before-upload="beforeAvatarUpload_Update"
                                            :headers="headers">
                                            <img v-if="imageUrl" :src="imageUrl" class="avatar">
                                            <i v-else class="el-icon-plus avatar-uploader-icon"></i>
                                        </el-upload>
                                    </el-form>

                                    <div slot="footer" class="dialog-footer">
                                        <el-button @click="dialogFormVisible = false">取 消</el-button>
                                        <el-button type="primary"
                                            @click="dialogFormVisible = false, formSubmission(form), formUpdateSuccess()">确
                                            定</el-button>
                                    </div>
                                </el-dialog>
                                <el-button 
                                type="danger" 
                                size="mini"
                                @click="handleDelete(scope.row.id)">删除</el-button>
                            </el-row>
                        </el-table-column>
                    </el-table>
                    <!--分页条-->
                    <el-pagination 
                    background layout="sizes,prev,pager,next,jumper,total" 
                        @size-change="handleSizeChange"
                        :page-sizes="[5, 10, 20, 50]" 
                        :page-size="pageSize" 
                        @current-change="handleCurrentChange"
                        :current-page="currentPage" 
                        :total="total">
                    </el-pagination>
                </el-main>
            </el-container>
        </el-container>


    </div>
</template>


<script>
/* import router from '@/router'; */
import axios from 'axios';
export default {
    data() {
        return {
            tableData: [],
            searchForm: {//查询表单的模型
                name: "",
                gender: "",
                /* dialogFormVisible: false, */
            },
            dialogFormVisible: false,//编辑表单的dialog内的数据模型
            dialogFormVisibleInsert: false,//新增表单的dialog内的数据模型
            dialogImageUrl: '',//编辑中图片的url
            dialogVisible: false,//编辑中图片的数据模型
            dialogVisible_Delete:false,
            form: {//编辑表单的模型
                id: "",
                name: '',
                gender: '',
                address: "",
                email: "",
                delivery: false,
                image: "",
            }, 
            deleteId:"",
            formInsert: {//添加学生的dialog内的数据模型
                id: "",
                name: '',
                password: "",
                gender: '',
                address: "",
                email: "",
                delivery: false,
                image: ""
            },
            formLabelWidth: '120px',
            pageSize: 5,//每页展示数据的行数的数据模型
            currentPage: 1,//页数模型
            total: 0,//计数，共计有多少条信息
            imageUrl: '',//照片上传的数据模型
        };
    },
    computed:{
        headers(){
            const token=sessionStorage.getItem("token")
            return {token:token}
        },
    },
    methods: {
        uploadGetId(id) {
            this.form.id = id;
        },
        onSubmit() {//学生信息查询的功能（在最上面的位置）
            let onSubmitJSON = {
                "name": this.searchForm.name,
                "gender": this.searchForm.gender
            }
            console.log(onSubmitJSON);
            axios.post("http://localhost:8044/emp/lookLike?name=", onSubmitJSON, {
                headers: {
                    "Content-Type": "application/json"
                }
            })
                .then((result) => {
                    this.tableData = result.data.data;
                })
        },
        returnLog(){
            window.sessionStorage.removeItem("token");
            this.$router.push({
                        path:'/log',
                    })
        },
        image(){
        },
        onSubmitInsert(formInsert) {//学生新增功能
            let getJson = JSON.stringify(formInsert);
            axios.post("http://localhost:8044/emp/Insert", getJson, {
                headers: {
                    "Content-Type": "application/json"
                }
            }).then(() => {
                this.flush(1,5)
            })
        },
        
        handleSizeChange(val){ //选择每页展示几条数据的方法
            console.log("每页记录数变化" + val);
            this.pageSize = val;
            axios.get("http://localhost:8044/emp/page?page="
                + this.currentPage + "&pageSize=" + val).then((result) => {
                    this.total = result.data.data.total;
                    this.tableData = result.data.data.rows;
                })
        },
        
        handleCurrentChange: function (val) {//换页的方法
            this.currentPage = val;
            axios.get("http://localhost:8044/emp/page?page="
                + val + "&pageSize=" + this.pageSize).then((result) => {
                    this.total = result.data.data.total
                    this.tableData = result.data.data.rows;
                })
        },
        
        flush(page,pageSize) {//刷新table表格的方法
            axios.get("http://localhost:8044/emp/page?page="
                + page+ "&pageSize=" + pageSize).then((result) => {
                    this.total = result.data.data.total;
                    this.tableData = result.data.data.rows;
                })
            //关于这里的刷新，流，虽然页数和展示数量都要在函数里编写形参，
            //但是在flush函数里面又默认设置前端表单的页面为1，以后这里记得要及时更改
            this.currentPage=1;
        },



        //删除学生信息的方法(内置了一个弹窗确认功能，同时还会报一个删除成功功能，后续可以根据后端服务器的返回值更改这里的删除成功参数进行调整)
        handleDelete(id) {
            let that=this
            this.$confirm("删除该学生信息。确定吗？","提示",{
                confirmButtonText:"确定",
                cancelButtonText:"取消",
                type:"warning"
            }).then(()=>{
                axios.delete("http://localhost:8044/student/" + id).
                then(()=>{
                    that.$message({
                        type:"success",
                        message:"删除成功"
                    });
                })
            }).then(()=>{
                this.flush(1,5)
            })
            
        },
        //学生删除成功的弹窗
/*         handleDeleteSuccess() {
            const h = this.$createElement;
            this.$message({
                message: h('p', null, [
                    h('span', null, '信息删除'),
                    h('i', { style: 'color: red' }, '成功')
                ])
            });
        }, */




        //学生编辑(更新)方法
        formSubmission(form) {
            console.log(this.form.image);
            let formJson = JSON.stringify(form);
            axios.post("http://localhost:8044/emp/update", formJson, {
                headers: {
                    "Content-Type": "application/json"
                }
            })
            this.flush(1,5);
        },
        //这是编辑成功后的弹窗
        formUpdateSuccess() {
            const h = this.$createElement;
            this.$message({
                message: h('p', null, [
                    h('span', null, '信息编辑'),
                    h('i', { style: 'color: teal' }, '成功')
                ])
            });
            this.flush(1,5);
        },
        //学生添加方法


        //图片上传的两个函数:在表单编辑中
        
        handleAvatarSuccess_Update(res, file) {
            this.imageUrl = URL.createObjectURL(file.raw);
            this.form.image = res.data;//将返回的照片的url赋值给form编辑表单中的对应变量去
        },
        beforeAvatarUpload_Update(file) {
            const isJPG = file.type === 'image/jpeg';
            const isLt2M = file.size / 1024 / 1024 < 10;

            if (!isJPG) {
                this.$message.error('上传头像图片只能是 JPG 格式!');
            }
            if (!isLt2M) {
                this.$message.error('上传头像图片大小不能超过 10MB!');
            }
            return isJPG && isLt2M;
        },
        handleAvatarSuccess_Insert(res, file) {
            this.imageUrl = URL.createObjectURL(file.raw);
            this.formInsert.image = res.data;//将返回的照片的url赋值给form编辑表单中的对应变量去
        },
        beforeAvatarUpload_Insert(file) {
            const isJPG = file.type === 'image/jpeg';
            const isLt2M = file.size / 1024 / 1024 < 10;
            if (!isJPG) {
                this.$message.error('上传图片只能是 JPG 格式!');
            }
            if (!isLt2M) {
                this.$message.error('上传图片大小不能超过 10MB!');
            }
            return isJPG && isLt2M;
        }
    },
    mounted() {
        // //发送异步请求
        // if(sessionStorage.length==0){
        //     this.$router.push({
        //                 path:'/log',
        //             })
        // }
        // axios({
        //     method:'get',
        //     url:"http://localhost:8044/emp/page",         
        // }).then((result) => {
        //     this.total = result.data.data.total;
        //     this.tableData = result.data.data.rows; 
        // })

    },
    created() {
    },
}

</script>



<style>
@import '@/assets/css/MainView.css';
.avatar-uploader .el-upload {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
}

.avatar-uploader .el-upload:hover {
    border-color: #409EFF;
}

.avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 178px;
    height: 178px;
    line-height: 178px;
    text-align: center;
}

.avatar {
    width: 178px;
    height: 178px;
    display: block;
}

</style>