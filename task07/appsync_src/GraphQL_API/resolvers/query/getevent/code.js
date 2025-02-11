import * as ddb from '@aws-appsync/utils/dynamodb';
import { util } from '@aws-appsync/utils';

export function request(ctx) {
    return {
        operation: "Query", // Uses 'Query' operation which might require GSI if 'id' is not the primary key
        query: {
            expression: 'id = :id',
            expressionValues: util.dynamodb.toMapValues({":id": ctx.args.id})
        }
    }
}

export function response(ctx) {
    if (ctx.error) {
        return util.error(ctx.error.message, ctx.error.type);
    }
    if (!ctx.result || !ctx.result.items || ctx.result.items.length === 0) {
        return util.error("Event not found.", "NotFoundError");
    }

    return ctx.result.items[0];
}