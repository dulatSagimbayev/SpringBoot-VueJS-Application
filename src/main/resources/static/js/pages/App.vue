<template>
    <v-app>
        <v-app-bar app>
            <v-toolbar-title>Network</v-toolbar-title>
            <v-spacer></v-spacer>
            <span v-if="profile">{{profile.name}}</span>
            <v-btn v-if="profile" icon href="/logout">
                <v-icon>exit_to_app</v-icon>
            </v-btn>

        </v-app-bar>
        <v-main>
            <v-container v-if="!profile">Необходимо авторизоваться через
                <a href="/login">Google</a>
            </v-container>
            <v-container v-if="profile">
                <messages-list :messages="messages" />
            </v-container>
        </v-main>





    </v-app>
</template>

<script>
    import MessagesList from 'components/messages/MessageList.vue'
    import { addHandler } from 'util/ws'
    export default {
        components: {
            MessagesList
        },
        data() {
            return {
                messages: frontendData.messages,
                profile: frontendData.profile
            }
        },
        created() {
            addHandler(data => {
                if(data.objectType=='MESSAGE'){
                    const index = this.messages.findIndex(item=>item.id==data.body.id)
                    switch(data.eventType){
                        case 'CREATE':
                        case 'UPDATE':
                            if(index>-1){
                                this.messages.splice(index,1,data.body)
                            }else{
                                this.messages.push(data.body)
                            }
                            break
                        case 'REMOVE':
                            this.messages.splice(index, 1)
                            break
                        default:
                            console.error('Event type is not valid')
                    }
                }
                else{
                    console.error('Object type is not valid')
                }
            })
        }
    }
</script>

<style>

</style>