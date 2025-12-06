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
                    <!-- 表格--->
                    <div v-if="isReloadData">
                        <el-table :data="tableData" border>
                            <el-table-column prop="id" label="id" width="200">
                            </el-table-column>

                            <el-table-column prop="class_name" label="姓名" width="200">
                                <template slot-scope="scope">
                                    {{ scope.row.name == null ? "无" : scope.row.name }}
                                </template>
                            </el-table-column>

                            <el-table-column prop="classNameMust1" label="必修1" width="200">
                                <template slot-scope="scope">
                                    {{ scope.row.name == null ? "无" : scope.row.ClassNameMust1 }}
                                </template>
                            </el-table-column>

                            <el-table-column prop="classNameMust2" label="必修2" width="200">
                                <template slot-scope="scope">
                                    {{ scope.row.name == null ? "无" : scope.row.ClassNameMust2 }}
                                </template>
                            </el-table-column>

                            <el-table-column prop="classNameChoose1" label="选修1" width="200">
                                <template slot-scope="scope">
                                    {{ scope.row.name == null ? "无" : scope.row.ClassNameChoose1 }}
                                </template>
                            </el-table-column>
<!--                             <el-table-column label="性别" width="200">
                                <template slot-scope="scope">
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
                            </el-table-column> -->
                        </el-table>
                    </div>
                    <el-pagination 
                        background layout="sizes,prev,pager,next,jumper,total" 
                        @size-change="handleSizeChange"
                        :page-sizes="[10,20,30,40,50,100]" 
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
            dialogFormVisible: false,
            form: {
                id: "",
                class_name: '',
                classNameMust1: '',
                classNameMust2: "",
                classNameChoose1: "",
                delivery: false
            },
            formLabelWidth: '120px',
            isReloadData: true,
            pageSize: 10,//每页展示数据的行数的数据模型
            currentPage: 1,//页数模型
            total: 0,//计数，共计有多少条信息
        };
    },
    methods: {
        getId(id) {
            this.form.id = id;
        },
        handleSizeChange(val){ //选择每页展示几条数据的方法
            console.log("每页记录数变化" + val);
            this.pageSize = val;
            axios.get("http://localhost:8044/class/page?page="
                + this.currentPage + "&pageSize=" + val).then((result) => {
                    this.total = result.data.data.total;
                    this.tableData = result.data.data.rows;
                })
        },
        handleCurrentChange: function (val) {//换页的方法
            this.currentPage = val;
            axios.get("http://localhost:8044/class/page?page="
                + val + "&pageSize=" + this.pageSize).then((result) => {
                    this.total = result.data.data.total
                    this.tableData = result.data.data.rows;
                })
        },
        reloadPart() {
            this.isReloadData = false
            this.$nextTick(() => {
                this.isReloadData = true
            })
        },
    },
    mounted() {
        // //发送异步请求
        // if(sessionStorage.length==0){
        //     this.$router.push({
        //                 path:'/log',
        //             })
        // }//如果会话存储长度为0则跳转界面至登陆界面
        // axios({
        //     method:'get',
        //     url:"http://localhost:8044/class/page?page=1&pageSize=10"         
        // }).then((result) => {
        //     this.total = result.data.data.total;
        //     this.tableData = result.data.data.rows; 
        // })
    },
    created() {
    },
}
</script>
<style scoped>
@import '@/assets/css/MainView.css';
</style>