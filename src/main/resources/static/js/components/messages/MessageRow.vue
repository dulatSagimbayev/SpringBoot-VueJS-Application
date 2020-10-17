<template>
    <v-card class="my-2">
        <v-card-text>
            <i>({{ message.id }})</i>
            {{ message.text }}
        </v-card-text>

        <v-card-actions>
            <v-btn  @click="edit" small rounded >
                Edit
            </v-btn>
            <v-btn icon @click="del" small>
                <v-icon>
                    delete
                </v-icon>
            </v-btn>
        </v-card-actions>
    </v-card>
</template>

<script>
    export default {
        props: ['message', 'editMessage', 'deleteMessage', 'messages'],
        methods: {
            edit() {
                this.editMessage(this.message)
            },
            del() {
                this.deleteMessage(this.message)
            }
        }
    }
</script>

<style>
</style>