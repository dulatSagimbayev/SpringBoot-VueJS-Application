<template>
    <v-app>
        <v-app-bar app>
            <v-toolbar-title class="ma-2">Network</v-toolbar-title>
            <v-btn color="secondary"
                   medium
                   outlined v-if="profile" :disabled="$route.path === '/'" @click="showMessages">
                Messages
            </v-btn>
            <v-spacer></v-spacer>
            <v-btn color="secondary"
                   medium
                   outlined v-if="profile" :disabled="$route.path === '/user'" @click="showProfile">{{profile.name}}</v-btn>
            <v-btn v-if="profile" icon href="/logout">
                <v-icon>exit_to_app</v-icon>
            </v-btn>

        </v-app-bar>
        <v-main>
            <router-view></router-view>
        </v-main>

    </v-app>
</template>

<script>
    import {mapState,mapMutations} from 'vuex'
    import { addHandler } from 'util/ws'

    export default {
        computed: mapState(['profile']),
        methods:{
            ...mapMutations(['addMessageMutation',
            'updateMessageMutation',
            'removeMessageMutation',
            'addCommentMutation']),

            showMessages(){
                this.$router.push('/')
            },
            showProfile(){
                this.$router.push('/user')
            }
        },
        created() {
            addHandler(data => {
                if(data.objectType==='MESSAGE'){
                    switch(data.eventType){
                        case 'CREATE':
                            this.addMessageMutation(data.body)
                            break
                        case 'UPDATE':
                            this.updateMessageMutation(data.body)
                            break
                        case 'REMOVE':
                            this.removeMessageMutation(data.body)
                            break
                        default:
                            console.error('Event type is not valid')
                    }
                }else if (data.objectType === 'COMMENT') {
                    switch (data.eventType) {
                        case 'CREATE':
                            this.addCommentMutation(data.body)
                            break
                        default:
                            console.error(`Looks like the event type if unknown "${data.eventType}"`)
                    }
              }
                else{
                    console.error('Object type is not valid')
                }
            })
        },
        beforeMount(){
            if(!this.profile){
                this.$router.replace('/auth')
            }
        }
    }
</script>

<style>

</style>